package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.CaseDefinition
import com.example.model.ChallengeModifier
import com.example.model.GameMode
import com.example.ui.screens.*
import com.example.ui.theme.AnomalyFilesTheme
import com.example.viewmodel.GameViewModel

object AnomalyRoutes {
    const val MAIN_MENU = "main_menu"
    const val HEADQUARTERS = "headquarters/{roomIndex}"
    const val CASE_BRIEFING = "case_briefing"
    const val INVESTIGATION_3D = "investigation_3d"
    const val INVESTIGATION_MENU = "investigation_menu"
    const val CASE_COMPLETION = "case_completion"

    fun hq(roomIndex: Int) = "headquarters/$roomIndex"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnomalyFilesTheme {
                AnomalyFilesApp()
            }
        }
    }
}

@Composable
fun AnomalyFilesApp(viewModel: GameViewModel = viewModel()) {
    val navController = rememberNavController()
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val caseProgress by viewModel.caseProgressState.collectAsStateWithLifecycle()
    val allEvidence by viewModel.allCollectedEvidence.collectAsStateWithLifecycle()
    val unlockedAchievements by viewModel.unlockedAchievements.collectAsStateWithLifecycle()
    val activeState by viewModel.activeInvestigation.collectAsStateWithLifecycle()
    val lastEvaluation by viewModel.lastEvaluation.collectAsStateWithLifecycle()

    var pendingBriefingCase by remember { mutableStateOf<CaseDefinition?>(null) }
    var pendingBriefingMode by remember { mutableStateOf(GameMode.STORY) }
    var pendingChallengeMod by remember { mutableStateOf<ChallengeModifier?>(null) }

    NavHost(
        navController = navController,
        startDestination = AnomalyRoutes.MAIN_MENU
    ) {
        composable(AnomalyRoutes.MAIN_MENU) {
            MainMenuScreen(
                profile = profile,
                caseProgress = caseProgress,
                unlockedAchievements = unlockedAchievements,
                onCompleteIntro = { viewModel.completeIntro() },
                onNewGame = { viewModel.startNewGame() },
                onContinueCase = { caseDef, mode, mod ->
                    pendingBriefingCase = caseDef
                    pendingBriefingMode = mode
                    pendingChallengeMod = mod
                    navController.navigate(AnomalyRoutes.CASE_BRIEFING)
                },
                onOpenHeadquarters = { room ->
                    navController.navigate(AnomalyRoutes.hq(room))
                },
                onSetDifficulty = { viewModel.setDifficulty(it) },
                onSetSound = { viewModel.setSoundEnabled(it) },
                onSetSensitivity = { viewModel.setLookSensitivity(it) }
            )
        }

        composable(
            route = AnomalyRoutes.HEADQUARTERS,
            arguments = listOf(navArgument("roomIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val roomIdx = backStackEntry.arguments?.getInt("roomIndex") ?: 0
            HeadquartersScreen(
                initialRoomIndex = roomIdx,
                profile = profile,
                caseProgress = caseProgress,
                allEvidence = allEvidence,
                onBack = { navController.popBackStack() },
                onDeployCase = { caseDef ->
                    pendingBriefingCase = caseDef
                    pendingBriefingMode = GameMode.STORY
                    pendingChallengeMod = null
                    navController.navigate(AnomalyRoutes.CASE_BRIEFING)
                },
                onPurchaseEquipment = { viewModel.purchaseEquipment(it) },
                onUpgradeBattery = { viewModel.upgradeBattery() },
                onSelectOutfit = { viewModel.selectOutfit(it) }
            )
        }

        composable(AnomalyRoutes.CASE_BRIEFING) {
            val caseDef = pendingBriefingCase
            if (caseDef != null) {
                CaseBriefingScreen(
                    caseDef = caseDef,
                    gameMode = pendingBriefingMode,
                    challengeModifier = pendingChallengeMod,
                    onBack = { navController.popBackStack() },
                    onTravelToLocation = {
                        viewModel.beginCase(
                            caseDef = caseDef,
                            mode = pendingBriefingMode,
                            modifier = pendingChallengeMod,
                            resetPreviousEvidence = true
                        )
                        navController.navigate(AnomalyRoutes.INVESTIGATION_3D) {
                            popUpTo(AnomalyRoutes.CASE_BRIEFING) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(AnomalyRoutes.INVESTIGATION_3D) {
            val state = activeState
            if (state != null) {
                val caseEvidence by viewModel.observeCaseEvidence(state.caseDef.id)
                    .collectAsStateWithLifecycle(initialValue = emptyList())

                Investigation3DScreen(
                    state = state,
                    profile = profile,
                    collectedEvidence = caseEvidence,
                    onMoveInput = { sx, fy -> viewModel.setMoveInput(sx, fy) },
                    onLookDelta = { dx, dy -> viewModel.adjustLook(dx, dy) },
                    onToggleCrouch = { viewModel.toggleCrouch() },
                    onToggleRun = { viewModel.toggleRun() },
                    onSelectEquipment = { viewModel.selectEquipment(it) },
                    onInteract = { viewModel.interactWithTarget() },
                    onCollectFromInspection = { obj, eq -> viewModel.collectEvidenceFromInspection(obj, eq) },
                    onCloseInspection = { viewModel.closeInspectionModal() },
                    onOpenInvestigationMenu = { navController.navigate(AnomalyRoutes.INVESTIGATION_MENU) },
                    onPauseChange = { viewModel.setPause(it) },
                    onRestartCase = {
                        viewModel.beginCase(
                            caseDef = state.caseDef,
                            mode = state.gameMode,
                            modifier = state.challengeModifier,
                            resetPreviousEvidence = true
                        )
                    },
                    onExitToMenu = {
                        viewModel.exitInvestigation()
                        navController.popBackStack(AnomalyRoutes.MAIN_MENU, inclusive = false)
                    }
                )
            }
        }

        composable(AnomalyRoutes.INVESTIGATION_MENU) {
            val state = activeState
            if (state != null) {
                val caseEvidence by viewModel.observeCaseEvidence(state.caseDef.id)
                    .collectAsStateWithLifecycle(initialValue = emptyList())
                val boardLinks by viewModel.observeBoardLinks(state.caseDef.id)
                    .collectAsStateWithLifecycle(initialValue = emptyList())

                InvestigationMenuScreen(
                    caseDef = state.caseDef,
                    collectedEvidence = caseEvidence,
                    boardLinks = boardLinks,
                    onLinkEvidence = { a, b -> viewModel.linkEvidenceOnBoard(state.caseDef.id, a, b) },
                    onRemoveLink = { viewModel.removeBoardLink(it) },
                    onSubmitReport = { submission ->
                        viewModel.submitCaseReport(submission) {
                            navController.navigate(AnomalyRoutes.CASE_COMPLETION) {
                                popUpTo(AnomalyRoutes.INVESTIGATION_3D) { inclusive = true }
                            }
                        }
                    },
                    onBackToScene = { navController.popBackStack() }
                )
            }
        }

        composable(AnomalyRoutes.CASE_COMPLETION) {
            val eval = lastEvaluation
            if (eval != null) {
                CaseCompletionScreen(
                    evaluation = eval,
                    onAcknowledgeTwist = { viewModel.markPostCase001TwistSeen() },
                    onReturnToHeadquarters = {
                        navController.navigate(AnomalyRoutes.hq(0)) {
                            popUpTo(AnomalyRoutes.MAIN_MENU) { inclusive = false }
                        }
                    },
                    onReturnToMenu = {
                        navController.popBackStack(AnomalyRoutes.MAIN_MENU, inclusive = false)
                    }
                )
            }
        }
    }
}
