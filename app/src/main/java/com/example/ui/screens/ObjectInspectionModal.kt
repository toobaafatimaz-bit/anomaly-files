package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.EquipmentType
import com.example.model.InteractiveObject
import com.example.model.ObjectKind
import com.example.ui.components.ClassifiedBadge
import com.example.ui.components.DossierPanel
import com.example.ui.theme.*

@Composable
fun ObjectInspectionModal(
    obj: InteractiveObject,
    initialEquipment: EquipmentType?,
    unlockedEquipmentNames: Set<String>,
    alreadyCollected: Boolean,
    onCollectEvidence: (EquipmentType?) -> Unit,
    onDismiss: () -> Unit
) {
    var activeFilter by remember { mutableStateOf(initialEquipment) }
    var rotY by remember { mutableFloatStateOf(0f) }
    var rotX by remember { mutableFloatStateOf(0f) }
    var zoom by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssalNavy.copy(alpha = 0.92f))
            .safeDrawingPadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        DossierPanel(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            borderColor = when (activeFilter) {
                EquipmentType.SPECTRAL_LENS -> SpectralVioletBright
                EquipmentType.THERMAL_VISION -> EmergencyRed
                EquipmentType.MEMORY_RECORDER -> ScannerCyan
                else -> BorderSlate
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ClassifiedBadge(
                        text = obj.kind.name.replace("_", " "),
                        color = ScannerCyan
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(obj.name, style = MaterialTheme.typography.headlineMedium, color = DossierTextPrimary)
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("close_inspection_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Inspection", tint = DossierTextMuted)
                }
            }

            Spacer(Modifier.height(10.dp))

            // 3D Rotatable & Zoomable Forensic Artifact Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .background(AbyssalNavy, RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            rotY = (rotY + dragAmount.x * 0.4f).coerceIn(-38f, 38f)
                            rotX = (rotX - dragAmount.y * 0.35f).coerceIn(-25f, 25f)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .size(width = 190.dp, height = 118.dp)
                        .graphicsLayer {
                            rotationY = rotY
                            rotationX = rotX
                            scaleX = zoom
                            scaleY = zoom
                            cameraDistance = 12f * density
                        },
                    shape = RoundedCornerShape(8.dp),
                    color = when (activeFilter) {
                        EquipmentType.SPECTRAL_LENS -> Color(0xFF2E1065)
                        EquipmentType.THERMAL_VISION -> Color(0xFF450A0A)
                        else -> ElevatedSlate
                    },
                    border = BorderStroke(
                        1.5.dp,
                        when (activeFilter) {
                            EquipmentType.SPECTRAL_LENS -> SpectralVioletBright
                            EquipmentType.THERMAL_VISION -> ClassifiedAmber
                            else -> ScannerCyan
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (obj.kind == ObjectKind.CLOCK) "03:17" else obj.name.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = ScannerCyan
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = when (activeFilter) {
                                EquipmentType.SPECTRAL_LENS -> obj.spectralDescription ?: "No hidden UV markings found."
                                EquipmentType.THERMAL_VISION -> obj.thermalDescription ?: "Normal ambient temperature."
                                EquipmentType.MEMORY_RECORDER -> obj.memoryEchoDescription ?: "No residual temporal echo."
                                EquipmentType.AUDIO_ANALYZER -> obj.audioSnippet ?: "Silent background floor."
                                else -> "Drag to rotate • Use zoom or switch lens below"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = DossierTextPrimary
                        )
                    }
                }
            }

            // Zoom slider
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom", tint = ScannerCyan)
                Slider(
                    value = zoom,
                    onValueChange = { zoom = it },
                    valueRange = 1.0f..1.85f,
                    modifier = Modifier.weight(1f)
                )
            }

            // Equipment Lens Filter Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filterTools = listOf(
                    null to "EYE",
                    EquipmentType.SPECTRAL_LENS to "SPC",
                    EquipmentType.THERMAL_VISION to "THM",
                    EquipmentType.AUDIO_ANALYZER to "AUD",
                    EquipmentType.MEMORY_RECORDER to "MEM"
                )
                filterTools.forEach { (eq, code) ->
                    val available = eq == null || eq.name in unlockedEquipmentNames
                    val selected = activeFilter == eq
                    FilterChip(
                        selected = selected,
                        enabled = available,
                        onClick = { activeFilter = eq },
                        label = { Text(code, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Detailed Observation & Witness Testimony
            Column(
                modifier = Modifier
                    .heightIn(max = 150.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(obj.baseDescription, style = MaterialTheme.typography.bodyMedium, color = DossierTextPrimary)
                if (obj.witnessStatement != null) {
                    Text(
                        "${obj.witnessName ?: "Witness"}: \"${obj.witnessStatement}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClassifiedAmber
                    )
                }
                if (activeFilter == EquipmentType.SPECTRAL_LENS && obj.spectralDescription != null) {
                    Text("SPECTRAL FINDING: ${obj.spectralDescription}", style = MaterialTheme.typography.bodyMedium, color = SpectralVioletBright)
                }
                if (activeFilter == EquipmentType.THERMAL_VISION && obj.thermalDescription != null) {
                    Text("THERMAL FINDING: ${obj.thermalDescription}", style = MaterialTheme.typography.bodyMedium, color = EmergencyRed)
                }
                if (activeFilter == EquipmentType.AUDIO_ANALYZER && obj.audioSnippet != null) {
                    Text("AUDIO EVP: ${obj.audioSnippet}", style = MaterialTheme.typography.bodyMedium, color = ScannerCyan)
                }
                if (activeFilter == EquipmentType.MEMORY_RECORDER && obj.memoryEchoDescription != null) {
                    Text("MEMORY ECHO: ${obj.memoryEchoDescription}", style = MaterialTheme.typography.bodyMedium, color = ScannerCyan)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (obj.linkedEvidenceId != null) {
                Button(
                    onClick = {
                        onCollectEvidence(activeFilter)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("collect_evidence_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (alreadyCollected) SignalEmerald else ScannerCyan
                    )
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = AbyssalNavy)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (alreadyCollected) "UPDATE FORENSIC LOG" else "COLLECT EVIDENCE TO DOSSIER",
                        style = MaterialTheme.typography.labelLarge,
                        color = AbyssalNavy
                    )
                }
            }
        }
    }
}
