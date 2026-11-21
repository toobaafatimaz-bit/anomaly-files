package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.cases.CaseCatalog
import com.example.data.cases.CaseCatalogPart2
import com.example.data.local.CaseProgressEntity
import com.example.data.local.PlayerProfileEntity
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class MenuSubPage {
    HOME,
    CASE_SELECTION,
    ENDLESS_MODE,
    CHALLENGE_MODE,
    ACHIEVEMENTS,
    SETTINGS
}

@Composable
fun MainMenuScreen(
    profile: PlayerProfileEntity,
    caseProgress: Map<String, CaseProgressEntity>,
    unlockedAchievements: Set<String>,
    onCompleteIntro: () -> Unit,
    onNewGame: () -> Unit,
    onContinueCase: (CaseDefinition, GameMode, ChallengeModifier?) -> Unit,
    onOpenHeadquarters: (Int) -> Unit,
    onSetDifficulty: (Difficulty) -> Unit,
    onSetSound: (Boolean) -> Unit,
    onSetSensitivity: (Float) -> Unit
) {
    if (!profile.introCompleted) {
        CinematicIntroView(onFinishIntro = onCompleteIntro)
        return
    }

    var subPage by remember { mutableStateOf(MenuSubPage.HOME) }
    var showNewGameConfirm by remember { mutableStateOf(false) }

    if (subPage != MenuSubPage.HOME) {
        BackHandler { subPage = MenuSubPage.HOME }
    }

    DossierScanlineBackground {
        when (subPage) {
            MenuSubPage.HOME -> {
                MainMenuHomeView(
                    profile = profile,
                    caseProgress = caseProgress,
                    onContinue = {
                        val nextStoryCase = CaseCatalogPart2.allStoryCases.firstOrNull { c ->
                            val prog = caseProgress[c.id]
                            (prog?.unlocked == true || c.id == "case_001") && prog?.completed != true
                        } ?: CaseCatalogPart2.allStoryCases.first()
                        onContinueCase(nextStoryCase, GameMode.STORY, null)
                    },
                    onNewGameRequest = { showNewGameConfirm = true },
                    onNavigateSubPage = { subPage = it },
                    onOpenHeadquarters = onOpenHeadquarters
                )
            }
            MenuSubPage.CASE_SELECTION -> {
                CaseSelectionView(
                    caseProgress = caseProgress,
                    onBack = { subPage = MenuSubPage.HOME },
                    onLaunchCase = { caseDef, mode -> onContinueCase(caseDef, mode, null) }
                )
            }
            MenuSubPage.ENDLESS_MODE -> {
                EndlessModeView(
                    onBack = { subPage = MenuSubPage.HOME },
                    onLaunchEndless = { seed ->
                        onContinueCase(CaseCatalogPart2.generateEndlessCase(seed), GameMode.ENDLESS, null)
                    }
                )
            }
            MenuSubPage.CHALLENGE_MODE -> {
                ChallengeModeView(
                    onBack = { subPage = MenuSubPage.HOME },
                    onLaunchChallenge = { caseDef, mod ->
                        onContinueCase(caseDef, GameMode.CHALLENGE, mod)
                    }
                )
            }
            MenuSubPage.ACHIEVEMENTS -> {
                AchievementsView(
                    unlockedIds = unlockedAchievements,
                    unlockedEndingsCsv = profile.unlockedEndingsCsv,
                    onBack = { subPage = MenuSubPage.HOME }
                )
            }
            MenuSubPage.SETTINGS -> {
                SettingsView(
                    profile = profile,
                    onDifficultyChange = onSetDifficulty,
                    onSoundChange = onSetSound,
                    onSensitivityChange = onSetSensitivity,
                    onBack = { subPage = MenuSubPage.HOME }
                )
            }
        }
    }

    if (showNewGameConfirm) {
        AlertDialog(
            onDismissRequest = { showNewGameConfirm = false },
            containerColor = DossierSlate,
            title = { Text("Reset Personnel File?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "Starting a New Game erases your current Story Mode case records, XP, and collected evidence to replay from the dark briefing room.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DossierTextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNewGameConfirm = false
                        onNewGame()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    modifier = Modifier.testTag("confirm_new_game_button")
                ) {
                    Text("RESET FILE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewGameConfirm = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
private fun CinematicIntroView(onFinishIntro: () -> Unit) {
    val fullVoiceLine = stringResource(R.string.intro_voice_line)
    var charCount by remember { mutableIntStateOf(0) }
    var showButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        for (i in 1..fullVoiceLine.length) {
            charCount = i
            delay(26L)
        }
        delay(350L)
        showButton = true
    }

    DossierScanlineBackground(accentGlow = EmergencyRed) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClassifiedBadge(text = "SECTOR 04 // INTERROGATION CELL", color = EmergencyRed)
                Text(
                    text = "03:17 AM",
                    style = MaterialTheme.typography.labelLarge,
                    color = ScannerCyan
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hq_briefing),
                        contentDescription = stringResource(R.string.cd_hq_banner),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, AbyssalNavy.copy(alpha = 0.85f))
                                )
                            )
                    )
                    Text(
                        text = "AUDIO PLAYBACK // UNKNOWN SPEAKER",
                        style = MaterialTheme.typography.labelSmall,
                        color = ScannerCyan,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    )
                }

                DossierPanel(borderColor = ScannerCyanDim) {
                    Text(
                        text = "\"${fullVoiceLine.take(charCount)}\"",
                        style = MaterialTheme.typography.titleLarge,
                        color = DossierTextPrimary
                    )
                }
            }

            AnimatedVisibility(visible = showButton, enter = fadeIn()) {
                Button(
                    onClick = onFinishIntro,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("enter_agency_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ScannerCyan)
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AbyssalNavy)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "ACKNOWLEDGE CLEARANCE // ENTER DUI",
                        style = MaterialTheme.typography.labelLarge,
                        color = AbyssalNavy
                    )
                }
            }
        }
    }
}

@Composable
private fun MainMenuHomeView(
    profile: PlayerProfileEntity,
    caseProgress: Map<String, CaseProgressEntity>,
    onContinue: () -> Unit,
    onNewGameRequest: () -> Unit,
    onNavigateSubPage: (MenuSubPage) -> Unit,
    onOpenHeadquarters: (Int) -> Unit
) {
    val completedCount = caseProgress.values.count { it.completed }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(195.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hq_briefing),
                contentDescription = stringResource(R.string.cd_hq_banner),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(AbyssalNavy.copy(alpha = 0.2f), AbyssalNavy.copy(alpha = 0.94f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClassifiedBadge(text = "DUI // SECTOR 04", color = EmergencyRed)
                    ClassifiedBadge(text = "${completedCount}/8 SOLVED", color = ScannerCyan)
                }
                Text(
                    text = "ANOMALY FILES",
                    style = MaterialTheme.typography.displayLarge,
                    color = DossierTextPrimary
                )
                Text(
                    text = stringResource(R.string.tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = DossierTextMuted
                )
            }
        }

        RankStatusChip(xp = profile.xp, credits = profile.credits, modifier = Modifier.fillMaxWidth())

        // Primary Action: Continue Investigation
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("continue_investigation_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ScannerCyan),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AbyssalNavy)
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (completedCount == 0) "BEGIN CASE #001: THE EMPTY APARTMENT" else "CONTINUE INVESTIGATION",
                style = MaterialTheme.typography.labelLarge,
                color = AbyssalNavy
            )
        }

        // Menu Action Grid / Stack
        MenuActionRow(
            icon = Icons.Default.Apartment,
            title = "Headquarters (DUI Sector 04)",
            subtitle = "Office, Armory, Evidence Lab, Personnel & Classified Room",
            accent = SpectralVioletBright,
            testTag = "menu_headquarters_button",
            onClick = { onOpenHeadquarters(0) }
        )
        MenuActionRow(
            icon = Icons.Default.FolderOpen,
            title = "Case Selection",
            subtitle = "Story Cases #001–#008 & Free Investigation",
            accent = ScannerCyan,
            testTag = "menu_case_selection_button",
            onClick = { onNavigateSubPage(MenuSubPage.CASE_SELECTION) }
        )
        MenuActionRow(
            icon = Icons.Default.AllInclusive,
            title = "Endless Investigation",
            subtitle = "Procedural anomalies with escalating intelligence",
            accent = ClassifiedAmber,
            testTag = "menu_endless_button",
            onClick = { onNavigateSubPage(MenuSubPage.ENDLESS_MODE) }
        )
        MenuActionRow(
            icon = Icons.Default.Warning,
            title = "Challenge Mode",
            subtitle = "Restricted equipment & hyper-adaptive entities",
            accent = EmergencyRed,
            testTag = "menu_challenge_button",
            onClick = { onNavigateSubPage(MenuSubPage.CHALLENGE_MODE) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompactMenuCard(
                icon = Icons.Default.Build,
                label = "Equipment",
                modifier = Modifier
                    .weight(1f)
                    .testTag("menu_equipment_button"),
                onClick = { onOpenHeadquarters(3) } // Jumps directly to Equipment Armory room in HQ
            )
            CompactMenuCard(
                icon = Icons.Default.EmojiEvents,
                label = "Achievements",
                modifier = Modifier
                    .weight(1f)
                    .testTag("menu_achievements_button"),
                onClick = { onNavigateSubPage(MenuSubPage.ACHIEVEMENTS) }
            )
            CompactMenuCard(
                icon = Icons.Default.Settings,
                label = "Settings",
                modifier = Modifier
                    .weight(1f)
                    .testTag("menu_settings_button"),
                onClick = { onNavigateSubPage(MenuSubPage.SETTINGS) }
            )
        }

        OutlinedButton(
            onClick = onNewGameRequest,
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("new_game_button"),
            border = BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.5f))
        ) {
            Text("NEW GAME (RESET FILE)", style = MaterialTheme.typography.labelLarge, color = EmergencyRed)
        }

        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun MenuActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag(testTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = PanelCharcoal,
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(accent.copy(alpha = 0.14f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = DossierTextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DossierTextMuted)
        }
    }
}

@Composable
private fun CompactMenuCard(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = PanelCharcoal,
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = label, tint = ScannerCyan)
            Text(label, style = MaterialTheme.typography.labelSmall, color = DossierTextPrimary)
        }
    }
}

@Composable
private fun CaseSelectionView(
    caseProgress: Map<String, CaseProgressEntity>,
    onBack: () -> Unit,
    onLaunchCase: (CaseDefinition, GameMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        DossierTopBar(title = "Case Files", subtitle = "Story & Free Investigation", onBack = onBack)
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(CaseCatalogPart2.allStoryCases, key = { it.id }) { caseDef ->
                val prog = caseProgress[caseDef.id]
                val isUnlocked = caseDef.id == "case_001" || (prog?.unlocked == true)
                DossierPanel(
                    borderColor = if (isUnlocked) ScannerCyanDim else BorderSlate.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(caseDef.caseNumber, style = MaterialTheme.typography.labelLarge, color = ScannerCyan)
                        if (prog?.completed == true) {
                            ClassifiedBadge(text = "RANK ${prog.bestRating}", color = SignalEmerald)
                        } else if (!isUnlocked) {
                            ClassifiedBadge(text = "LOCKED", color = DossierTextSubtle)
                        } else {
                            ClassifiedBadge(text = caseDef.threatClassification.name, color = EmergencyRed)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(caseDef.title, style = MaterialTheme.typography.headlineMedium, color = DossierTextPrimary)
                    Text(caseDef.locationName, style = MaterialTheme.typography.labelSmall, color = DossierTextMuted)
                    Spacer(Modifier.height(8.dp))
                    Text(caseDef.incidentSummary, style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                    Spacer(Modifier.height(12.dp))
                    if (isUnlocked) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onLaunchCase(caseDef, GameMode.STORY) },
                                modifier = Modifier.weight(1f).testTag("launch_${caseDef.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = ScannerCyan)
                            ) {
                                Text("INVESTIGATE", color = AbyssalNavy, style = MaterialTheme.typography.labelLarge)
                            }
                            if (prog?.completed == true) {
                                OutlinedButton(
                                    onClick = { onLaunchCase(caseDef, GameMode.FREE) },
                                    modifier = Modifier.weight(1f).testTag("free_${caseDef.id}")
                                ) {
                                    Text("FREE EXPLORE", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EndlessModeView(
    onBack: () -> Unit,
    onLaunchEndless: (Int) -> Unit
) {
    var seed by remember { mutableIntStateOf((1000..9999).random()) }
    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        DossierTopBar(title = "Endless Investigation", subtitle = "Procedural Incident Generator", onBack = onBack)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            DossierPanel(borderColor = ClassifiedAmber) {
                ClassifiedBadge(text = "PROCEDURAL DISPATCH #$seed", color = ClassifiedAmber)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Endless Investigation synthesizes randomized anomaly classifications, architectural environments, and adaptive entities. Submit accurate reports to earn bonus Investigation Credits.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DossierTextPrimary
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { seed = (1000..9999).random() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("REROLL SEED")
                    }
                    Button(
                        onClick = { onLaunchEndless(seed) },
                        modifier = Modifier.weight(1f).testTag("start_endless_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ClassifiedAmber)
                    ) {
                        Text("DEPLOY", color = AbyssalNavy, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeModeView(
    onBack: () -> Unit,
    onLaunchChallenge: (CaseDefinition, ChallengeModifier) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        DossierTopBar(title = "Challenge Mode", subtitle = "Restricted Field Protocols", onBack = onBack)
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(ChallengeModifier.entries) { mod ->
                DossierPanel(borderColor = EmergencyRed.copy(alpha = 0.7f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(mod.title, style = MaterialTheme.typography.titleLarge, color = DossierTextPrimary)
                        ClassifiedBadge(text = "+${mod.bonusCredits} CR", color = ClassifiedAmber)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(mod.description, style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { onLaunchChallenge(CaseCatalogPart2.allStoryCases.first(), mod) },
                        modifier = Modifier.fillMaxWidth().testTag("challenge_${mod.name}"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                    ) {
                        Text("START PROTOCOL", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementsView(
    unlockedIds: Set<String>,
    unlockedEndingsCsv: String,
    onBack: () -> Unit
) {
    val unlockedEndings = unlockedEndingsCsv.split(",").filter { it.isNotBlank() }.toSet()
    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        DossierTopBar(
            title = "Achievements & Endings",
            subtitle = "${unlockedIds.size}/${CaseCatalog.allAchievements.size} Medals • ${unlockedEndings.size}/6 Endings",
            onBack = onBack
        )
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(CaseCatalog.allAchievements) { ach ->
                val done = ach.id in unlockedIds
                DossierPanel(borderColor = if (done) SignalEmerald else BorderSlate) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(
                            imageVector = if (done) Icons.Default.EmojiEvents else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (done) SignalEmerald else DossierTextSubtle
                        )
                        Column {
                            Text(ach.title, style = MaterialTheme.typography.titleLarge, color = DossierTextPrimary)
                            Text(ach.description, style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("DISCOVERED ENDINGS", style = MaterialTheme.typography.labelLarge, color = SpectralVioletBright)
            }
            items(EndingType.entries) { ending ->
                val seen = ending.name in unlockedEndings
                DossierPanel(borderColor = if (seen) SpectralViolet else BorderSlate) {
                    Text(
                        text = if (seen) ending.title else "CLASSIFIED ENDING [LOCKED]",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (seen) SpectralVioletBright else DossierTextSubtle
                    )
                    if (seen) {
                        Spacer(Modifier.height(4.dp))
                        Text(ending.narrative, style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsView(
    profile: PlayerProfileEntity,
    onDifficultyChange: (Difficulty) -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        DossierTopBar(title = "Settings", subtitle = "Field Hardware Calibration", onBack = onBack)
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DossierPanel {
                Text("DIFFICULTY PROTOCOL", style = MaterialTheme.typography.labelLarge, color = ScannerCyan)
                Spacer(Modifier.height(10.dp))
                Difficulty.entries.forEach { diff ->
                    val selected = profile.difficulty == diff.name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onDifficultyChange(diff) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selected, onClick = { onDifficultyChange(diff) })
                        Column {
                            Text(diff.label, style = MaterialTheme.typography.titleLarge)
                            Text(diff.description, style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                        }
                    }
                }
            }

            DossierPanel {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Procedural Audio & EVP", style = MaterialTheme.typography.titleLarge)
                        Text("Footsteps, EMF ticks, and anomaly warnings", style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                    }
                    Switch(checked = profile.soundEnabled, onCheckedChange = onSoundChange)
                }
            }

            DossierPanel {
                Text("Camera Look Sensitivity: ${"%.1f".format(profile.lookSensitivity)}x", style = MaterialTheme.typography.titleLarge)
                Slider(
                    value = profile.lookSensitivity,
                    onValueChange = onSensitivityChange,
                    valueRange = 0.5f..2.0f
                )
            }
        }
    }
}
