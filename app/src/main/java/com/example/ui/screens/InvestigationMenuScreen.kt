package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.BoardLinkEntity
import com.example.data.local.CollectedEvidenceEntity
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InvestigationMenuScreen(
    caseDef: CaseDefinition,
    collectedEvidence: List<CollectedEvidenceEntity>,
    boardLinks: List<BoardLinkEntity>,
    onLinkEvidence: (String, String) -> Unit,
    onRemoveLink: (String) -> Unit,
    onSubmitReport: (CaseReportSubmission) -> Unit,
    onBackToScene: () -> Unit
) {
    BackHandler(onBack = onBackToScene)

    val tabs = listOf("Evidence DB", "Investigation Board", "Case Report")
    var selectedTab by remember { mutableIntStateOf(0) }

    val collectedIds = remember(collectedEvidence) { collectedEvidence.map { it.evidenceId }.toSet() }
    val discoveredDefs = remember(caseDef, collectedIds) {
        caseDef.evidencePool.filter { it.id in collectedIds }
    }

    DossierScanlineBackground {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            DossierTopBar(
                title = "Investigation Dossier",
                subtitle = "${caseDef.caseNumber} // ${caseDef.title}",
                onBack = onBackToScene
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DossierSlate,
                contentColor = ScannerCyan
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(title, style = MaterialTheme.typography.labelLarge) },
                        modifier = Modifier.testTag("inv_tab_$idx")
                    )
                }
            }

            when (selectedTab) {
                0 -> EvidenceDatabaseTab(discoveredDefs, collectedEvidence)
                1 -> InvestigationBoardTab(
                    caseDef = caseDef,
                    discoveredDefs = discoveredDefs,
                    boardLinks = boardLinks,
                    onLinkEvidence = onLinkEvidence,
                    onRemoveLink = onRemoveLink
                )
                2 -> CaseReportTab(
                    caseDef = caseDef,
                    discoveredDefs = discoveredDefs,
                    onSubmitReport = onSubmitReport
                )
            }
        }
    }
}

@Composable
private fun EvidenceDatabaseTab(
    discoveredDefs: List<EvidenceDefinition>,
    collectedRows: List<CollectedEvidenceEntity>
) {
    var selectedCategory by remember { mutableStateOf<EvidenceCategory?>(null) }
    val filtered = if (selectedCategory == null) discoveredDefs else discoveredDefs.filter { it.category == selectedCategory }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("ALL (${discoveredDefs.size})") }
                )
            }
            items(EvidenceCategory.entries) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.label) }
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                DossierPanel {
                    Text("NO EVIDENCE LOGGED", style = MaterialTheme.typography.titleLarge, color = ScannerCyan)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Explore the 3D location, crouch near floorboards, and examine photographs, clocks, mirrors, and terminals to collect evidence.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DossierTextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { ev ->
                    val meta = collectedRows.find { it.evidenceId == ev.id }
                    DossierPanel(
                        borderColor = when {
                            ev.isConspiracySecret -> SpectralVioletBright
                            ev.isMisleadingRedHerring -> ClassifiedAmber
                            else -> ScannerCyanDim
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ClassifiedBadge(
                                text = ev.category.label,
                                color = if (ev.isConspiracySecret) SpectralVioletBright else ScannerCyan
                            )
                            Text(ev.anomalyCategory.label, style = MaterialTheme.typography.labelSmall, color = DossierTextMuted)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(ev.title, style = MaterialTheme.typography.titleLarge, color = DossierTextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(ev.summary, style = MaterialTheme.typography.bodyMedium, color = DossierTextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text("Forensic Analysis: ${ev.detailedAnalysis}", style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                        if (meta?.photographed == true || meta?.spectralAnalyzed == true) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                buildString {
                                    if (meta.photographed) append("[PHOTOGRAPHED] ")
                                    if (meta.spectralAnalyzed) append("[SPECTRAL VERIFIED]")
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = SignalEmerald
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvestigationBoardTab(
    caseDef: CaseDefinition,
    discoveredDefs: List<EvidenceDefinition>,
    boardLinks: List<BoardLinkEntity>,
    onLinkEvidence: (String, String) -> Unit,
    onRemoveLink: (String) -> Unit
) {
    var firstSelectedId by remember { mutableStateOf<String?>(null) }
    val linkedEvidenceSet = remember(boardLinks) {
        boardLinks.flatMap { listOf(it.evidenceA, it.evidenceB) }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (firstSelectedId == null) {
                "Tap any two collected clues below to draw an investigative thread and test hypotheses."
            } else {
                "Select a second clue to connect with '${discoveredDefs.find { it.id == firstSelectedId }?.title}'."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = ScannerCyan
        )

        // Visual Node Pinboard Canvas
        if (discoveredDefs.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(210.dp),
                shape = RoundedCornerShape(8.dp),
                color = AbyssalNavy,
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val n = discoveredDefs.size
                        val cx = size.width * 0.5f
                        val cy = size.height * 0.5f
                        val rx = size.width * 0.36f
                        val ry = size.height * 0.34f

                        val positions = discoveredDefs.mapIndexed { idx, ev ->
                            val angle = (2.0 * Math.PI * idx / n) - Math.PI / 2.0
                            ev.id to Offset(
                                cx + (cos(angle) * rx).toFloat(),
                                cy + (sin(angle) * ry).toFloat()
                            )
                        }.toMap()

                        boardLinks.forEach { link ->
                            val p1 = positions[link.evidenceA]
                            val p2 = positions[link.evidenceB]
                            if (p1 != null && p2 != null) {
                                drawLine(
                                    color = EmergencyRed,
                                    start = p1,
                                    end = p2,
                                    strokeWidth = 3f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
                                )
                            }
                        }

                        positions.forEach { (id, pos) ->
                            val selected = id == firstSelectedId
                            drawCircle(
                                color = if (selected) ClassifiedAmber else ScannerCyan,
                                radius = if (selected) 13f else 9f,
                                center = pos
                            )
                        }
                    }

                    Text(
                        text = "${boardLinks.size} ACTIVE THREAD(S)",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmergencyRed,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    )
                }
            }
        }

        // Clickable Clue Nodes
        discoveredDefs.forEach { ev ->
            val isPicked = firstSelectedId == ev.id
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("board_node_${ev.id}")
                    .clickable {
                        val current = firstSelectedId
                        if (current == null) {
                            firstSelectedId = ev.id
                        } else if (current == ev.id) {
                            firstSelectedId = null
                        } else {
                            onLinkEvidence(current, ev.id)
                            firstSelectedId = null
                        }
                    },
                shape = RoundedCornerShape(8.dp),
                color = if (isPicked) ElevatedSlate else PanelCharcoal,
                border = BorderStroke(1.5.dp, if (isPicked) ClassifiedAmber else BorderSlate)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ev.title, style = MaterialTheme.typography.titleMedium, color = DossierTextPrimary)
                        Text(ev.category.label, style = MaterialTheme.typography.labelSmall, color = DossierTextMuted)
                    }
                    Icon(
                        Icons.Default.Link,
                        contentDescription = "Link Clue",
                        tint = if (isPicked) ClassifiedAmber else ScannerCyan
                    )
                }
            }
        }

        // Active Thread Links
        if (boardLinks.isNotEmpty()) {
            Text("CONNECTED CLUE THREADS", style = MaterialTheme.typography.labelLarge, color = EmergencyRed)
            boardLinks.forEach { link ->
                val titleA = discoveredDefs.find { it.id == link.evidenceA }?.title ?: link.evidenceA
                val titleB = discoveredDefs.find { it.id == link.evidenceB }?.title ?: link.evidenceB
                DossierPanel(borderColor = EmergencyRed.copy(alpha = 0.6f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$titleA  +  $titleB", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onRemoveLink(link.id) }) {
                            Text("UNLINK", color = EmergencyRed)
                        }
                    }
                }
            }
        }

        // Hypothesis Synthesis Results
        Text("HYPOTHESIS EVALUATION", style = MaterialTheme.typography.labelLarge, color = SignalEmerald)
        caseDef.validTheories.forEach { theory ->
            val unlocked = theory.requiredEvidenceIds.all { it in linkedEvidenceSet }
            if (unlocked) {
                DossierPanel(borderColor = if (theory.isValidDeduction) SignalEmerald else EmergencyRed) {
                    ClassifiedBadge(
                        text = if (theory.isValidDeduction) "SUPPORTED THEORY" else "CONTRADICTED HYPOTHESIS",
                        color = if (theory.isValidDeduction) SignalEmerald else EmergencyRed
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(theory.title, style = MaterialTheme.typography.titleLarge)
                    Text(theory.conclusion, style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                }
            }
        }
    }
}

@Composable
private fun CaseReportTab(
    caseDef: CaseDefinition,
    discoveredDefs: List<EvidenceDefinition>,
    onSubmitReport: (CaseReportSubmission) -> Unit
) {
    var classification by remember { mutableStateOf(AnomalyClassification.UNKNOWN_ANOMALY) }
    var threatLevel by remember { mutableStateOf(caseDef.threatClassification) }
    var action by remember { mutableStateOf(RecommendedAction.CONTAIN) }
    var selectedEvidenceIds by remember(discoveredDefs) {
        mutableStateOf(discoveredDefs.filter { !it.isMisleadingRedHerring }.map { it.id }.toSet())
    }
    var explanation by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        DossierPanel {
            Text("1. ANOMALY CLASSIFICATION", style = MaterialTheme.typography.labelLarge, color = ScannerCyan)
            Spacer(Modifier.height(8.dp))
            AnomalyClassification.entries.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { classification = item }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = classification == item, onClick = { classification = item })
                    Column {
                        Text(item.label, style = MaterialTheme.typography.titleMedium)
                        Text(item.threatSummary, style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                    }
                }
            }
        }

        DossierPanel {
            Text("2. THREAT CLASSIFICATION", style = MaterialTheme.typography.labelLarge, color = EmergencyRed)
            Spacer(Modifier.height(8.dp))
            ThreatLevel.entries.forEach { lvl ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { threatLevel = lvl }.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = threatLevel == lvl, onClick = { threatLevel = lvl })
                    Text(lvl.label, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        DossierPanel {
            Text("3. RECOMMENDED DUI ACTION", style = MaterialTheme.typography.labelLarge, color = ClassifiedAmber)
            Spacer(Modifier.height(8.dp))
            RecommendedAction.entries.forEach { act ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { action = act }.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = action == act, onClick = { action = act })
                    Text(act.label, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        DossierPanel {
            Text("4. SUPPORTING EVIDENCE (${selectedEvidenceIds.size} attached)", style = MaterialTheme.typography.labelLarge, color = SignalEmerald)
            Spacer(Modifier.height(6.dp))
            if (discoveredDefs.isEmpty()) {
                Text("No evidence collected yet—you can still submit, but accuracy will suffer.", color = EmergencyRed, style = MaterialTheme.typography.bodyMedium)
            } else {
                discoveredDefs.forEach { ev ->
                    val checked = ev.id in selectedEvidenceIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedEvidenceIds = if (checked) selectedEvidenceIds - ev.id else selectedEvidenceIds + ev.id
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                selectedEvidenceIds = if (checked) selectedEvidenceIds - ev.id else selectedEvidenceIds + ev.id
                            }
                        )
                        Text(ev.title, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        OutlinedTextField(
            value = explanation,
            onValueChange = { explanation = it },
            label = { Text("Investigator Summary Notes (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Button(
            onClick = {
                onSubmitReport(
                    CaseReportSubmission(
                        caseId = caseDef.id,
                        classification = classification,
                        threatLevel = threatLevel,
                        recommendedAction = action,
                        selectedEvidenceIds = selectedEvidenceIds,
                        theoryExplanation = explanation
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("submit_report_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ScannerCyan)
        ) {
            Icon(Icons.Default.Send, contentDescription = null, tint = AbyssalNavy)
            Spacer(Modifier.width(8.dp))
            Text("SUBMIT OFFICIAL CASE REPORT", style = MaterialTheme.typography.labelLarge, color = AbyssalNavy)
        }
    }
}
