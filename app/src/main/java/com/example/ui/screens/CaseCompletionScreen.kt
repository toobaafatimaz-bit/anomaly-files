package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.cases.CaseCatalogPart2
import com.example.model.CaseEvaluationResult
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun CaseCompletionScreen(
    evaluation: CaseEvaluationResult,
    onAcknowledgeTwist: () -> Unit,
    onReturnToHeadquarters: () -> Unit,
    onReturnToMenu: () -> Unit
) {
    BackHandler(onBack = onReturnToHeadquarters)

    val caseDef = remember(evaluation.caseId) { CaseCatalogPart2.getCaseById(evaluation.caseId) }
    var twistDismissed by remember { mutableStateOf(!evaluation.triggerPostCase001Twist) }

    DossierScanlineBackground(
        accentGlow = if (evaluation.unlockedEnding != null) SpectralViolet else ScannerCyan
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!twistDismissed) {
                // Iconic Post-Case #001 Personnel Photograph Reveal!
                ClassifiedBadge(text = "SECTOR 04 // PERSONAL OFFICE ANOMALY", color = EmergencyRed)
                Text("WAIT... LOOK AT YOUR BADGE PHOTOGRAPH", style = MaterialTheme.typography.headlineLarge, color = EmergencyRed)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_personnel_photo),
                        contentDescription = stringResource(R.string.cd_personnel_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                DossierPanel(borderColor = SpectralViolet) {
                    Text(
                        "Case #001 is closed. Yet back at Headquarters, you notice an unidentified figure standing directly behind your shoulder in your own personnel photograph. When you ask your supervisor, they stare at the frame and insist nobody else is there.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DossierTextPrimary
                    )
                }

                Button(
                    onClick = {
                        onAcknowledgeTwist()
                        twistDismissed = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("acknowledge_twist_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SpectralViolet)
                ) {
                    Text("FILE OBSERVATION & VIEW CASE GRADE", style = MaterialTheme.typography.labelLarge, color = AbyssalNavy)
                }
            } else {
                // Ending Resolution Banner if unlocked
                if (evaluation.unlockedEnding != null) {
                    DossierPanel(borderColor = SpectralVioletBright) {
                        ClassifiedBadge(text = "STORY CONVERGENCE REACHED", color = SpectralVioletBright)
                        Spacer(Modifier.height(6.dp))
                        Text(evaluation.unlockedEnding.title, style = MaterialTheme.typography.headlineLarge, color = SpectralVioletBright)
                        Text(evaluation.unlockedEnding.subtitle, style = MaterialTheme.typography.titleMedium, color = ScannerCyan)
                        Spacer(Modifier.height(8.dp))
                        Text(evaluation.unlockedEnding.narrative, style = MaterialTheme.typography.bodyLarge, color = DossierTextPrimary)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        ClassifiedBadge(text = "CASE DEBRIEF COMPLETE", color = ScannerCyan)
                        Spacer(Modifier.height(4.dp))
                        Text(caseDef?.title ?: evaluation.caseId, style = MaterialTheme.typography.headlineLarge)
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (evaluation.rating == "F") EmergencyRed else ScannerCyan
                    ) {
                        Text(
                            text = evaluation.rating,
                            style = MaterialTheme.typography.displayLarge,
                            color = AbyssalNavy,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                        )
                    }
                }

                DossierPanel {
                    StatLine("Classification Accuracy", if (evaluation.classificationCorrect) "VERIFIED" else "INCORRECT")
                    StatLine("Overall Accuracy", "${evaluation.accuracyPercent}%")
                    StatLine("Core Evidence Collected", "${evaluation.evidenceFoundCount} / ${evaluation.totalCoreEvidenceCount}")
                    StatLine("Classified Secret Found", if (evaluation.secretDiscovered) "YES (+60 CR)" else "NO")
                    StatLine("Time Taken", "${evaluation.timeTakenSeconds}s")
                    StatLine("Witness Safety", "${evaluation.witnessSafetyScore}%")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderSlate)
                    StatLine("XP Earned", "+${evaluation.xpEarned} XP")
                    StatLine("Credits Earned", "+${evaluation.creditsEarned} CR")
                }

                DossierPanel(borderColor = ScannerCyanDim) {
                    Text("EVALUATOR NOTES", style = MaterialTheme.typography.labelLarge, color = ScannerCyan)
                    Spacer(Modifier.height(4.dp))
                    Text(evaluation.feedbackSummary, style = MaterialTheme.typography.bodyMedium, color = DossierTextPrimary)
                    if (caseDef != null) {
                        Spacer(Modifier.height(8.dp))
                        Text("CONSPIRACY THREAD: ${caseDef.conspiracyRevealText}", style = MaterialTheme.typography.bodyMedium, color = SpectralVioletBright)
                    }
                    if (evaluation.newlyUnlockedCaseTitle != null) {
                        Spacer(Modifier.height(8.dp))
                        ClassifiedBadge(text = "UNLOCKED: ${evaluation.newlyUnlockedCaseTitle}", color = SignalEmerald)
                    }
                }

                Button(
                    onClick = onReturnToHeadquarters,
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("return_to_hq_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ScannerCyan)
                ) {
                    Icon(Icons.Default.Apartment, contentDescription = null, tint = AbyssalNavy)
                    Spacer(Modifier.width(8.dp))
                    Text("RETURN TO HEADQUARTERS", style = MaterialTheme.typography.labelLarge, color = AbyssalNavy)
                }

                OutlinedButton(
                    onClick = onReturnToMenu,
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("return_to_main_menu_button")
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("MAIN MENU", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
        Text(value, style = MaterialTheme.typography.titleMedium, color = DossierTextPrimary)
    }
}
