package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CaseDefinition
import com.example.model.ChallengeModifier
import com.example.model.GameMode
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun CaseBriefingScreen(
    caseDef: CaseDefinition,
    gameMode: GameMode,
    challengeModifier: ChallengeModifier?,
    onBack: () -> Unit,
    onTravelToLocation: () -> Unit
) {
    BackHandler(onBack = onBack)

    DossierScanlineBackground(accentGlow = EmergencyRed) {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            DossierTopBar(
                title = caseDef.caseNumber,
                subtitle = "${gameMode.label} // Assignment Briefing",
                onBack = onBack,
                trailing = {
                    ClassifiedBadge(text = caseDef.threatClassification.name, color = EmergencyRed)
                }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Image(
                        painter = painterResource(
                            id = if (caseDef.id == "case_001") R.drawable.img_case_apartment else R.drawable.img_anomaly_silhouette
                        ),
                        contentDescription = stringResource(R.string.cd_case_apartment),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, AbyssalNavy.copy(alpha = 0.9f))))
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    ) {
                        Text(caseDef.title, style = MaterialTheme.typography.headlineLarge, color = DossierTextPrimary)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = ScannerCyan, modifier = Modifier.size(16.dp))
                            Text(caseDef.locationName, style = MaterialTheme.typography.bodyMedium, color = ScannerCyan)
                        }
                    }
                }

                if (challengeModifier != null) {
                    DossierPanel(borderColor = EmergencyRed) {
                        ClassifiedBadge(text = "CHALLENGE PROTOCOL: ${challengeModifier.title}", color = EmergencyRed)
                        Spacer(Modifier.height(4.dp))
                        Text(challengeModifier.description, style = MaterialTheme.typography.bodyMedium, color = DossierTextPrimary)
                    }
                }

                DossierPanel {
                    Text("INCIDENT REPORT", style = MaterialTheme.typography.labelLarge, color = ScannerCyan)
                    Spacer(Modifier.height(6.dp))
                    Text(caseDef.incidentSummary, style = MaterialTheme.typography.bodyLarge, color = DossierTextPrimary)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = ClassifiedAmber, modifier = Modifier.size(18.dp))
                        Text("Reported Witnesses: ${caseDef.witnessCount}", style = MaterialTheme.typography.titleMedium, color = ClassifiedAmber)
                    }
                }

                DossierPanel {
                    Text("KNOWN SUSPICIOUS ACTIVITIES", style = MaterialTheme.typography.labelLarge, color = EmergencyRed)
                    Spacer(Modifier.height(6.dp))
                    caseDef.suspiciousActivities.forEach { act ->
                        Text("• $act", style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                        Spacer(Modifier.height(3.dp))
                    }
                }

                DossierPanel(borderColor = ScannerCyanDim) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = ScannerCyan, modifier = Modifier.size(18.dp))
                        Text("PRIMARY OBJECTIVE", style = MaterialTheme.typography.labelLarge, color = ScannerCyan)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(caseDef.primaryObjective, style = MaterialTheme.typography.bodyLarge, color = DossierTextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text("OPTIONAL SECRET: ${caseDef.optionalSecretObjective}", style = MaterialTheme.typography.bodyMedium, color = SpectralVioletBright)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onTravelToLocation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("deploy_to_location_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ScannerCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AbyssalNavy)
                    Spacer(Modifier.width(8.dp))
                    Text("TRAVEL TO LOCATION", style = MaterialTheme.typography.labelLarge, color = AbyssalNavy)
                }
            }
        }
    }
}
