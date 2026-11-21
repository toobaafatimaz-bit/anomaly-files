package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.local.CollectedEvidenceEntity
import com.example.data.local.PlayerProfileEntity
import com.example.engine.FirstPersonEngine
import com.example.model.EquipmentType
import com.example.ui.components.ClassifiedBadge
import com.example.ui.components.DossierPanel
import com.example.ui.theme.*
import com.example.viewmodel.ActiveInvestigationState
import kotlin.math.*

@Composable
fun Investigation3DScreen(
    state: ActiveInvestigationState,
    profile: PlayerProfileEntity,
    collectedEvidence: List<CollectedEvidenceEntity>,
    onMoveInput: (Float, Float) -> Unit,
    onLookDelta: (Float, Float) -> Unit,
    onToggleCrouch: () -> Unit,
    onToggleRun: () -> Unit,
    onSelectEquipment: (EquipmentType?) -> Unit,
    onInteract: () -> Unit,
    onCollectFromInspection: (com.example.model.InteractiveObject, EquipmentType?) -> Unit,
    onCloseInspection: () -> Unit,
    onOpenInvestigationMenu: () -> Unit,
    onPauseChange: (Boolean) -> Unit,
    onRestartCase: () -> Unit,
    onExitToMenu: () -> Unit
) {
    BackHandler {
        if (state.inspectedObject != null) {
            onCloseInspection()
        } else {
            onPauseChange(!state.isPaused)
        }
    }

    val collectedIds = remember(collectedEvidence) { collectedEvidence.map { it.evidenceId }.toSet() }
    val unlockedGear = remember(profile.unlockedEquipmentCsv) {
        profile.unlockedEquipmentCsv.split(",").filter { it.isNotBlank() }.toSet()
    }
    var showControlsDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(AbyssalNavy)) {
        // 1. REAL-TIME FIRST-PERSON 3D VIEWPORT + TOUCH LOOK
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onLookDelta(dragAmount.x, dragAmount.y)
                    }
                }
        ) {
            FirstPersonEngine.renderScene(
                drawScope = this,
                caseDef = state.caseDef,
                player = state.player,
                openDoors = state.openDoors,
                entities = state.entities,
                activeEquipment = state.activeEquipment,
                scannerPulseProgress = state.scannerPulseProgress,
                adaptation = state.adaptation,
                collectedEvidenceIds = collectedIds,
                animTimeSec = state.animTimeSec
            )
        }

        // Camera flash overlay
        if (state.cameraFlashAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = state.cameraFlashAlpha))
            )
        }

        // Center Reticle
        Box(modifier = Modifier.align(Alignment.Center)) {
            val targetHighlighted = state.lookTarget != null
            Box(
                modifier = Modifier
                    .size(if (targetHighlighted) 16.dp else 8.dp)
                    .border(
                        width = if (targetHighlighted) 2.dp else 1.5.dp,
                        color = if (targetHighlighted) ScannerCyan else Color.White.copy(alpha = 0.65f),
                        shape = CircleShape
                    )
            )
        }

        // Equipment-Specific Viewfinder Overlays
        if (state.activeEquipment == EquipmentType.AUDIO_ANALYZER) {
            AudioWaveformOverlay(
                emfStrength = state.emfStrength,
                animTime = state.animTimeSec,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 74.dp)
            )
        } else if (state.activeEquipment == EquipmentType.SIGNAL_DETECTOR) {
            SignalDetectorOverlay(
                emfStrength = state.emfStrength,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 74.dp)
            )
        }

        // 2. TOP HUD BAR
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Case & Evidence pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AbyssalNavy.copy(alpha = 0.82f),
                    border = BorderStroke(1.dp, BorderSlate)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column {
                            Text(state.caseDef.caseNumber, style = MaterialTheme.typography.labelSmall, color = ScannerCyan)
                            Text(
                                "CLUES ${collectedIds.size}/${state.caseDef.evidencePool.count { !it.isMisleadingRedHerring }}",
                                style = MaterialTheme.typography.labelLarge,
                                color = DossierTextPrimary
                            )
                        }
                        if (state.timeRemainingSeconds != null) {
                            val mins = state.timeRemainingSeconds / 60
                            val secs = state.timeRemainingSeconds % 60
                            ClassifiedBadge(
                                text = "%02d:%02d".format(mins, secs),
                                color = if (state.timeRemainingSeconds < 60) EmergencyRed else ClassifiedAmber
                            )
                        }
                    }
                }

                // Mini-Radar + Battery + Pause
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniBlueprintRadar(state = state)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AbyssalNavy.copy(alpha = 0.82f),
                        border = BorderStroke(1.dp, BorderSlate)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                "BAT ${state.batteryPercent.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.batteryPercent < 25f) EmergencyRed else ScannerCyan
                            )
                            Text(
                                "SAN ${state.healthSanityPercent.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = SignalEmerald
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenInvestigationMenu,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .background(ScannerCyan, RoundedCornerShape(8.dp))
                            .testTag("open_dossier_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = "Investigation Menu",
                            tint = AbyssalNavy
                        )
                    }

                    IconButton(
                        onClick = { onPauseChange(true) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .background(PanelCharcoal.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                            .testTag("pause_button")
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", tint = DossierTextPrimary)
                    }
                }
            }

            // Objective & Adaptation HUD Banner
            if (state.hudBannerMessage != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AbyssalNavy.copy(alpha = 0.84f),
                    border = BorderStroke(
                        1.dp,
                        if (state.adaptation.adaptationWarnings.isNotEmpty()) EmergencyRed else ScannerCyanDim
                    )
                ) {
                    Text(
                        text = state.hudBannerMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.hudBannerMessage.startsWith("ADAPTATION")) EmergencyRed else ScannerCyan,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // 3. CONTEXTUAL INTERACT PROMPT (above equipment bar)
        val lookTarget = state.lookTarget
        if (lookTarget != null) {
            Button(
                onClick = onInteract,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 66.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("interact_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ScannerCyan),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = if (lookTarget.isDoor) Icons.Default.MeetingRoom else Icons.Default.Search,
                    contentDescription = null,
                    tint = AbyssalNavy
                )
                Spacer(Modifier.width(6.dp))
                Text(lookTarget.promptText.uppercase(), color = AbyssalNavy, style = MaterialTheme.typography.labelLarge)
            }
        }

        // 4. BOTTOM CONTROLS (Joystick Left + Stance Right + Equipment Bar)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Virtual Movement Joystick
                VirtualJoystick(
                    onMove = onMoveInput,
                    modifier = Modifier.testTag("movement_joystick")
                )

                // Stance & Quick Action Controls (Right side)
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = onToggleRun,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (state.player.isRunning) ClassifiedAmber else PanelCharcoal.copy(alpha = 0.85f),
                                contentColor = if (state.player.isRunning) AbyssalNavy else DossierTextPrimary
                            ),
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("run_toggle_button")
                        ) {
                            Text("RUN", style = MaterialTheme.typography.labelLarge)
                        }
                        FilledTonalButton(
                            onClick = onToggleCrouch,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (state.player.isCrouching) SpectralVioletBright else PanelCharcoal.copy(alpha = 0.85f),
                                contentColor = if (state.player.isCrouching) AbyssalNavy else DossierTextPrimary
                            ),
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("crouch_toggle_button")
                        ) {
                            Text("CROUCH", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Text(
                        text = "Drag screen to look • ${state.caseDef.primaryObjective}",
                        style = MaterialTheme.typography.labelSmall,
                        color = DossierTextPrimary.copy(alpha = 0.8f),
                        modifier = Modifier
                            .background(AbyssalNavy.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Equipment Selection Strip
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(EquipmentType.entries) { eq ->
                    val unlocked = eq.name in unlockedGear
                    val selected = state.activeEquipment == eq
                    Surface(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("equip_${eq.shortCode}")
                            .clickable(enabled = unlocked) { onSelectEquipment(eq) },
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            selected -> ScannerCyan
                            unlocked -> PanelCharcoal.copy(alpha = 0.9f)
                            else -> AbyssalNavy.copy(alpha = 0.6f)
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                selected -> ScannerCyan
                                unlocked -> BorderSlate
                                else -> BorderSlate.copy(alpha = 0.3f)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = eq.shortCode,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) AbyssalNavy else if (unlocked) DossierTextPrimary else DossierTextSubtle
                            )
                            Text(
                                text = if (unlocked) eq.label.substringBefore(" ") else "LOCKED",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) AbyssalNavy else DossierTextMuted
                            )
                        }
                    }
                }
            }
        }

        // 5. OBJECT INSPECTION MODAL
        val inspected = state.inspectedObject
        if (inspected != null) {
            ObjectInspectionModal(
                obj = inspected,
                initialEquipment = state.activeEquipment,
                unlockedEquipmentNames = unlockedGear,
                alreadyCollected = inspected.linkedEvidenceId in collectedIds,
                onCollectEvidence = { eq -> onCollectFromInspection(inspected, eq) },
                onDismiss = onCloseInspection
            )
        }

        // 6. PAUSE MENU OVERLAY
        if (state.isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AbyssalNavy.copy(alpha = 0.88f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                DossierPanel(
                    modifier = Modifier.widthIn(max = 360.dp).padding(24.dp),
                    borderColor = ScannerCyan
                ) {
                    Text("INVESTIGATION PAUSED", style = MaterialTheme.typography.headlineMedium, color = ScannerCyan)
                    Text("${state.caseDef.caseNumber} — ${state.caseDef.title}", style = MaterialTheme.typography.bodyMedium, color = DossierTextMuted)
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { onPauseChange(false) },
                        modifier = Modifier.fillMaxWidth().testTag("resume_case_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ScannerCyan)
                    ) {
                        Text("RESUME", color = AbyssalNavy, style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onPauseChange(false); onOpenInvestigationMenu() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("INVESTIGATION BOARD & REPORT")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showControlsDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CONTROLS & EQUIPMENT GUIDE")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onRestartCase,
                        modifier = Modifier.fillMaxWidth().testTag("restart_case_button")
                    ) {
                        Text("RESTART CASE")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onExitToMenu,
                        modifier = Modifier.fillMaxWidth().testTag("exit_to_menu_button"),
                        border = BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.6f))
                    ) {
                        Text("ABORT TO MAIN MENU", color = EmergencyRed)
                    }
                }
            }
        }

        if (showControlsDialog) {
            AlertDialog(
                onDismissRequest = { showControlsDialog = false },
                containerColor = DossierSlate,
                title = { Text("Field Investigation Controls") },
                text = {
                    Text(
                        "• Left Joystick: Walk through rooms\n• Drag Anywhere: Look around 360°\n• CROUCH: Inspect floor seams & low clues\n• Equipment Bar: Toggle SCN / THM / AUD / MEM / SPC / CAM / EMF\n• Caution: Overusing one tool causes Intelligent Anomalies to adapt!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DossierTextMuted
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showControlsDialog = false }) { Text("CLOSE") }
                }
            )
        }
    }
}

@Composable
private fun VirtualJoystick(
    onMove: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadiusPx = 95f

    Box(
        modifier = modifier
            .size(116.dp)
            .clip(CircleShape)
            .background(AbyssalNavy.copy(alpha = 0.55f))
            .border(1.5.dp, ScannerCyan.copy(alpha = 0.55f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                ) { change, dragAmount ->
                    change.consume()
                    val candidate = knobOffset + dragAmount
                    val len = hypot(candidate.x, candidate.y)
                    val clamped = if (len > maxRadiusPx) {
                        Offset(candidate.x / len * maxRadiusPx, candidate.y / len * maxRadiusPx)
                    } else candidate
                    knobOffset = clamped
                    onMove(clamped.x / maxRadiusPx, -clamped.y / maxRadiusPx)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffset.x.roundToInt(), knobOffset.y.roundToInt()) }
                .size(44.dp)
                .clip(CircleShape)
                .background(ScannerCyan.copy(alpha = 0.85f))
        )
    }
}

@Composable
private fun MiniBlueprintRadar(state: ActiveInvestigationState) {
    Canvas(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(AbyssalNavy.copy(alpha = 0.85f))
            .border(1.dp, BorderSlate, RoundedCornerShape(6.dp))
    ) {
        val grid = state.caseDef.mapGrid
        val rows = grid.size
        val cols = grid.firstOrNull()?.length ?: 1
        val cellW = size.width / cols
        val cellH = size.height / rows

        for (y in grid.indices) {
            for (x in grid[y].indices) {
                val c = grid[y][x]
                if (c == '#' || c == 'M') {
                    drawRect(
                        color = if (c == 'M') SpectralViolet else BorderSlate,
                        topLeft = Offset(x * cellW, y * cellH),
                        size = Size(cellW, cellH)
                    )
                } else if ((c == 'D' || c == 'S') && (x to y) !in state.openDoors) {
                    drawRect(
                        color = if (c == 'S') EmergencyRed else ClassifiedAmber,
                        topLeft = Offset(x * cellW, y * cellH),
                        size = Size(cellW, cellH)
                    )
                }
            }
        }
        val px = state.player.x * cellW
        val py = state.player.y * cellH
        drawCircle(ScannerCyan, radius = 3.2f, center = Offset(px, py))
        drawLine(
            ScannerCyan,
            Offset(px, py),
            Offset(px + cos(state.player.yaw) * 8f, py + sin(state.player.yaw) * 8f),
            strokeWidth = 1.8f
        )
    }
}

@Composable
private fun AudioWaveformOverlay(emfStrength: Float, animTime: Float, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = AbyssalNavy.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, ScannerCyan)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("AUDIO ANALYZER // EVP SPECTRUM", style = MaterialTheme.typography.labelSmall, color = ScannerCyan)
            Canvas(modifier = Modifier.width(180.dp).height(28.dp)) {
                val bars = 24
                val bw = size.width / bars
                for (i in 0 until bars) {
                    val amp = (abs(sin(animTime * 8f + i * 0.7f)) * (0.3f + emfStrength * 0.7f)).coerceIn(0.1f, 1f)
                    val bh = size.height * amp
                    drawRect(
                        color = if (amp > 0.72f) EmergencyRed else ScannerCyan,
                        topLeft = Offset(i * bw + 1f, (size.height - bh) * 0.5f),
                        size = Size(bw - 2f, bh)
                    )
                }
            }
        }
    }
}

@Composable
private fun SignalDetectorOverlay(emfStrength: Float, modifier: Modifier = Modifier) {
    val level = (emfStrength * 100).toInt()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = AbyssalNavy.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, if (level > 65) EmergencyRed else ClassifiedAmber)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Sensors, contentDescription = null, tint = if (level > 65) EmergencyRed else ClassifiedAmber)
            Text("EMF SIGNAL: $level%", style = MaterialTheme.typography.labelLarge, color = DossierTextPrimary)
        }
    }
}
