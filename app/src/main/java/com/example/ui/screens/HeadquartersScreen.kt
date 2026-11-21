package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.cases.CaseCatalogPart2
import com.example.data.local.CaseProgressEntity
import com.example.data.local.CollectedEvidenceEntity
import com.example.data.local.PlayerProfileEntity
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HeadquartersScreen(
    initialRoomIndex: Int,
    profile: PlayerProfileEntity,
    caseProgress: Map<String, CaseProgressEntity>,
    allEvidence: List<CollectedEvidenceEntity>,
    onBack: () -> Unit,
    onDeployCase: (CaseDefinition) -> Unit,
    onPurchaseEquipment: (EquipmentType) -> Unit,
    onUpgradeBattery: () -> Unit,
    onSelectOutfit: (String) -> Unit
) {
    BackHandler(onBack = onBack)

    val rooms = listOf(
        "Personal Office",
        "Mission Room",
        "Evidence Lab",
        "Armory & Gear",
        "Archive",
        "Personnel Room",
        "Classified Room"
    )
    var selectedRoom by remember { mutableIntStateOf(initialRoomIndex.coerceIn(0, rooms.lastIndex)) }
    val completedCount = caseProgress.values.count { it.completed }
    val suspiciousLevel = completedCount + profile.conspiracyScore

    DossierScanlineBackground(
        accentGlow = if (suspiciousLevel >= 3) SpectralViolet else ScannerCyan
    ) {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            DossierTopBar(
                title = "DUI Headquarters",
                subtitle = if (suspiciousLevel >= 3) "SECTOR 04 // ANOMALOUS TELEMETRY DETECTED" else "SECTOR 04 // COMMAND",
                onBack = onBack,
                trailing = {
                    ClassifiedBadge(text = "${profile.credits} CR", color = ClassifiedAmber)
                }
            )

            // Room Selector Tabs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(rooms) { index, label ->
                    val selected = selectedRoom == index
                    FilterChip(
                        selected = selected,
                        onClick = { selectedRoom = index },
                        label = {
                            Text(label, style = MaterialTheme.typography.labelLarge)
                        },
                        modifier = Modifier.testTag("hq_room_$index"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ScannerCyan,
                            selectedLabelColor = AbyssalNavy
                        )
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            when (selectedRoom) {
                0 -> PersonalOfficeRoom(profile, completedCount, suspiciousLevel)
                1 -> MissionAssignmentRoom(caseProgress, onDeployCase)
                2 -> EvidenceLabRoom(allEvidence)
                3 -> ArmoryRoom(profile, onPurchaseEquipment, onUpgradeBattery, onSelectOutfit)
                4 -> ArchiveRoom(caseProgress, profile)
                5 -> PersonnelRecordsRoom(profile, suspiciousLevel)
                6 -> ClassifiedRoom(profile, caseProgress)
            }
        }
    }
}

@Composable
private fun PersonalOfficeRoom(
    profile: PlayerProfileEntity,
    completedCount: Int,
    suspiciousLevel: Int
) {
    val rank = DetectiveRank.fromXp(profile.xp)
    var spectralInspectPhoto by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        RankStatusChip(xp = profile.xp, credits = profile.credits, modifier = Modifier.fillMaxWidth())

        DossierPanel(borderColor = if (completedCount >= 1) SpectralViolet else ScannerCyanDim) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("INVESTIGATOR ID PHOTOGRAPH", style = MaterialTheme.typography.labelLarge, color = ScannerCyan)
                Switch(
                    checked = spectralInspectPhoto,
                    onCheckedChange = { spectralInspectPhoto = it }
                )
            }
            Text(
                text = if (spectralInspectPhoto) "SPECTRAL FILTER ACTIVE" else "Standard Optical View (Toggle UV Filter)",
                style = MaterialTheme.typography.labelSmall,
                color = if (spectralInspectPhoto) SpectralVioletBright else DossierTextMuted
            )
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.img_personnel_photo),
                    contentDescription = stringResource(R.string.cd_personnel_photo),
                    contentScale = ContentScale.Crop,
                    colorFilter = if (spectralInspectPhoto) {
                        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.2f) })
                    } else null,
                    modifier = Modifier
                        .width(120.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("CODE: ${profile.investigatorCode}", style = MaterialTheme.typography.titleMedium, color = ScannerCyan)
                    Text("RANK: ${rank.title}", style = MaterialTheme.typography.bodyMedium, color = DossierTextPrimary)
                    Text("OUTFIT: ${profile.activeOutfit}", style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = when {
                            completedCount == 0 -> "Standard issue badge photo. Nothing unusual appears on file."
                            spectralInspectPhoto -> "UV OVERLAY: The figure behind your shoulder holds a badge reading 'SUBJECT 050 — DO NOT ERASE'."
                            else -> "Wait... standing behind your shoulder in the dark is an unidentified silhouette. Nobody else in HQ can see it."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (completedCount >= 1) EmergencyRed else DossierTextMuted
                    )
                }
            }
        }

        if (suspiciousLevel >= 2) {
            DossierPanel(borderColor = EmergencyRed) {
                ClassifiedBadge(text = "OFFICE ENVIRONMENT ANOMALY", color = EmergencyRed)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Your office wall clock is stuck at 03:17. Every sticky note on your corkboard is written in your own handwriting—but you don't remember writing any of them.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DossierTextPrimary
                )
            }
        }
    }
}

@Composable
private fun MissionAssignmentRoom(
    caseProgress: Map<String, CaseProgressEntity>,
    onDeployCase: (CaseDefinition) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(CaseCatalogPart2.allStoryCases, key = { it.id }) { caseDef ->
            val prog = caseProgress[caseDef.id]
            val unlocked = caseDef.id == "case_001" || (prog?.unlocked == true)
            DossierPanel(borderColor = if (unlocked) ScannerCyanDim else BorderSlate) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(caseDef.caseNumber, style = MaterialTheme.typography.labelLarge, color = ScannerCyan)
                    ClassifiedBadge(
                        text = if (prog?.completed == true) "SOLVED (${prog.bestRating})" else if (unlocked) "ACTIVE" else "LOCKED",
                        color = if (prog?.completed == true) SignalEmerald else if (unlocked) ClassifiedAmber else DossierTextSubtle
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(caseDef.title, style = MaterialTheme.typography.titleLarge)
                Text(caseDef.locationName, style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                if (unlocked) {
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { onDeployCase(caseDef) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ScannerCyan)
                    ) {
                        Text("OPEN BRIEFING", color = AbyssalNavy, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun EvidenceLabRoom(allEvidence: List<CollectedEvidenceEntity>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("SUPERNATURAL ENTITY TAXONOMY", style = MaterialTheme.typography.labelLarge, color = SpectralVioletBright)
        }
        items(EntityType.entries) { entity ->
            DossierPanel(borderColor = SpectralViolet.copy(alpha = 0.6f)) {
                Text(entity.entityName, style = MaterialTheme.typography.titleLarge, color = SpectralVioletBright)
                Spacer(Modifier.height(4.dp))
                Text("Behavior: ${entity.behaviorSummary}", style = MaterialTheme.typography.bodyMedium, color = DossierTextPrimary)
                Text("Detection: ${entity.detectionMethod}", style = MaterialTheme.typography.bodyMedium, color = ScannerCyan)
                Text("Weakness: ${entity.weakness}", style = MaterialTheme.typography.bodyMedium, color = SignalEmerald)
            }
        }
        item {
            Spacer(Modifier.height(6.dp))
            Text("COLLECTED SPECIMENS (${allEvidence.size})", style = MaterialTheme.typography.labelLarge, color = ScannerCyan)
        }
        if (allEvidence.isEmpty()) {
            item {
                DossierPanel {
                    Text("No field evidence logged yet. Investigate a case to populate the lab database.", style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                }
            }
        } else {
            items(allEvidence) { row ->
                val def = CaseCatalogPart2.getCaseById(row.caseId)?.evidencePool?.find { it.id == row.evidenceId }
                if (def != null) {
                    DossierPanel {
                        Text(def.title, style = MaterialTheme.typography.titleLarge)
                        Text("${def.category.label} • ${def.anomalyCategory.label}", style = MaterialTheme.typography.labelSmall, color = ScannerCyan)
                        Spacer(Modifier.height(4.dp))
                        Text(def.summary, style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArmoryRoom(
    profile: PlayerProfileEntity,
    onPurchaseEquipment: (EquipmentType) -> Unit,
    onUpgradeBattery: () -> Unit,
    onSelectOutfit: (String) -> Unit
) {
    val unlockedSet = profile.unlockedEquipmentCsv.split(",").filter { it.isNotBlank() }.toSet()
    val outfits = listOf("Standard DUI Trench", "Night Ops Charcoal", "Spectral Containment Suit", "Director Overcoat")
    val nextBatteryCost = profile.batteryUpgradeLevel * 100

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DossierPanel(borderColor = ScannerCyan) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Field Battery Cell Lv.${profile.batteryUpgradeLevel}", style = MaterialTheme.typography.titleLarge)
                        Text("Max Capacity: ${100 + (profile.batteryUpgradeLevel - 1) * 20}%", style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                    }
                    if (profile.batteryUpgradeLevel < 4) {
                        Button(
                            onClick = onUpgradeBattery,
                            enabled = profile.credits >= nextBatteryCost,
                            colors = ButtonDefaults.buttonColors(containerColor = ClassifiedAmber)
                        ) {
                            Text("UPGRADE ($nextBatteryCost CR)", color = AbyssalNavy, style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        ClassifiedBadge(text = "MAXED", color = SignalEmerald)
                    }
                }
            }
        }

        item {
            Text("DETECTIVE EQUIPMENT", style = MaterialTheme.typography.labelLarge, color = ScannerCyan)
        }
        items(EquipmentType.entries) { eq ->
            val owned = eq.name in unlockedSet
            DossierPanel(borderColor = if (owned) SignalEmerald.copy(alpha = 0.6f) else ClassifiedAmber) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${eq.label} [${eq.shortCode}]", style = MaterialTheme.typography.titleLarge)
                        Text(eq.description, style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                    }
                    Spacer(Modifier.width(8.dp))
                    if (owned) {
                        ClassifiedBadge(text = "EQUIPPED", color = SignalEmerald)
                    } else {
                        Button(
                            onClick = { onPurchaseEquipment(eq) },
                            enabled = profile.credits >= eq.unlockCost,
                            colors = ButtonDefaults.buttonColors(containerColor = ClassifiedAmber)
                        ) {
                            Text("${eq.unlockCost} CR", color = AbyssalNavy, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        item {
            Text("INVESTIGATOR ATTIRE", style = MaterialTheme.typography.labelLarge, color = SpectralVioletBright)
        }
        items(outfits) { outfit ->
            val selected = profile.activeOutfit == outfit
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectOutfit(outfit) },
                shape = RoundedCornerShape(8.dp),
                color = if (selected) ElevatedSlate else PanelCharcoal,
                border = BorderStroke(1.dp, if (selected) ScannerCyan else BorderSlate)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(outfit, style = MaterialTheme.typography.titleMedium)
                    if (selected) ClassifiedBadge(text = "ACTIVE", color = ScannerCyan)
                }
            }
        }
    }
}

@Composable
private fun ArchiveRoom(
    caseProgress: Map<String, CaseProgressEntity>,
    profile: PlayerProfileEntity
) {
    val completed = caseProgress.values.filter { it.completed }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (completed.isEmpty()) {
            item {
                DossierPanel {
                    Text("Archive is empty. Complete Case #001 to seal your first investigation report.", color = DossierTextMuted)
                }
            }
        } else {
            items(completed) { prog ->
                val title = CaseCatalogPart2.getCaseById(prog.caseId)?.title ?: prog.caseId
                DossierPanel {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(title, style = MaterialTheme.typography.titleLarge)
                        ClassifiedBadge(text = "RANK ${prog.bestRating}", color = SignalEmerald)
                    }
                    Text(
                        "Accuracy: ${prog.bestAccuracy}% • Classification: ${prog.lastClassification}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DossierTextMuted
                    )
                }
            }
        }
        item {
            DossierPanel(borderColor = SpectralViolet) {
                Text("DISCOVERED ENDINGS", style = MaterialTheme.typography.labelLarge, color = SpectralVioletBright)
                Text(
                    profile.unlockedEndingsCsv.ifBlank { "None unlocked yet. Complete the Story Mode conspiracy." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = DossierTextMuted
                )
            }
        }
    }
}

@Composable
private fun PersonnelRecordsRoom(profile: PlayerProfileEntity, suspiciousLevel: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DossierPanel(borderColor = EmergencyRed) {
            ClassifiedBadge(text = "PERSONNEL RECORD // AGENT-049", color = EmergencyRed)
            Spacer(Modifier.height(8.dp))
            Text("Induction Date: [REDACTED — YEAR OVERWRITTEN]", style = MaterialTheme.typography.titleMedium, color = ScannerCyan)
            Text("Previous Assignment: [CLASSIFIED — MEMORY RESET ADMINISTERED]", style = MaterialTheme.typography.bodyMedium, color = DossierTextPrimary)
            Spacer(Modifier.height(8.dp))
            Text(
                if (suspiciousLevel >= 3) {
                    "WARNING: Cross-referencing payroll reveals Agents 001 through 048 share your exact fingerprints."
                } else {
                    "Note: All prior field reports before tonight's awakening have been sealed by Director order."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = EmergencyRed
            )
        }
    }
}

@Composable
private fun ClassifiedRoom(
    profile: PlayerProfileEntity,
    caseProgress: Map<String, CaseProgressEntity>
) {
    val secretsCount = caseProgress.values.count { it.secretDiscovered }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (secretsCount < 1) {
            DossierPanel(borderColor = EmergencyRedDim) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = EmergencyRed)
                Spacer(Modifier.height(8.dp))
                Text("CLEARANCE INSUFFICIENT", style = MaterialTheme.typography.titleLarge, color = EmergencyRed)
                Text(
                    "Uncover at least 1 hidden conspiracy clue (crouch near floorboards or use Spectral Lens inside unlisted rooms) to decrypt the Classified Room.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DossierTextMuted
                )
            }
        } else {
            DossierPanel(borderColor = SpectralViolet) {
                ClassifiedBadge(text = "DECRYPTED CONSPIRACY FRAGMENTS ($secretsCount)", color = SpectralVioletBright)
                Spacer(Modifier.height(8.dp))
                Text(
                    "PROJECT MIRROR-0 BRIEF: The Department of Unusual Incidents does not merely contain anomalies—it studies how investigators perceive impossible spaces. Subject 050 escaped containment during the Blackwood incident and began leaving clues inside your own files.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DossierTextPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Conspiracy Score: ${profile.conspiracyScore}  |  Department Loyalty: ${profile.loyaltyScore}",
                    style = MaterialTheme.typography.labelLarge,
                    color = ScannerCyan
                )
            }
        }
    }
}
