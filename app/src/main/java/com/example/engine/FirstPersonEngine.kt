package com.example.engine

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.*
import kotlin.math.*

data class RuntimeEntity(
    val def: SceneEntity,
    val x: Float,
    val y: Float,
    val patrolIndex: Int = 0,
    val vanished: Boolean = false,
    val glitchPhase: Float = 0f
)

data class Player3DState(
    val x: Float = 1.5f,
    val y: Float = 1.5f,
    val yaw: Float = 0f, // radians
    val pitchOffset: Float = 0f, // vertical look offset (-0.35..0.35)
    val isCrouching: Boolean = false,
    val isRunning: Boolean = false,
    val bobPhase: Float = 0f
)

object FirstPersonEngine {

    private const val FOV = 1.05f // ~60 deg horizontal FOV
    private const val RAY_COUNT = 140

    fun findSpawnPosition(mapGrid: List<String>): Pair<Float, Float> {
        for (y in mapGrid.indices) {
            for (x in mapGrid[y].indices) {
                if (mapGrid[y][x] == 'P') {
                    return (x + 0.5f) to (y + 0.5f)
                }
            }
        }
        return 1.5f to 1.5f
    }

    fun isWalkable(
        x: Float,
        y: Float,
        mapGrid: List<String>,
        openDoors: Set<Pair<Int, Int>>,
        margin: Float = 0.22f
    ): Boolean {
        val checks = listOf(
            x - margin to y - margin,
            x + margin to y - margin,
            x - margin to y + margin,
            x + margin to y + margin
        )
        for ((cx, cy) in checks) {
            val gx = floor(cx).toInt()
            val gy = floor(cy).toInt()
            if (gy !in mapGrid.indices || gx !in mapGrid[gy].indices) return false
            val tile = mapGrid[gy][gx]
            when (tile) {
                '#', 'M' -> return false
                'D', 'S' -> if ((gx to gy) !in openDoors) return false
            }
        }
        return true
    }

    fun findLookTarget(
        player: Player3DState,
        objects: List<InteractiveObject>,
        openDoors: Set<Pair<Int, Int>>,
        maxDist: Float = 2.35f
    ): InteractiveObject? {
        val dirX = cos(player.yaw)
        val dirY = sin(player.yaw)
        var bestObj: InteractiveObject? = null
        var bestScore = Float.MAX_VALUE

        for (obj in objects) {
            val dx = obj.gridX - player.x
            val dy = obj.gridY - player.y
            val dist = hypot(dx, dy)
            if (dist > maxDist || dist < 0.15f) continue
            // If it's an already-open door, allow closing or skip if player is standing inside it
            if (obj.isDoor && dist < 0.45f && (obj.doorGridX to obj.doorGridY) in openDoors) continue

            val dot = (dx / dist) * dirX + (dy / dist) * dirY
            if (dot > 0.72f) {
                val score = dist * (1.25f - dot)
                if (score < bestScore) {
                    bestScore = score
                    bestObj = obj
                }
            }
        }
        return bestObj
    }

    fun calculateEmfNearLook(
        player: Player3DState,
        objects: List<InteractiveObject>,
        entities: List<RuntimeEntity>
    ): Float {
        val dirX = cos(player.yaw)
        val dirY = sin(player.yaw)
        var peak = 0.08f
        for (obj in objects) {
            if (obj.emfStrength <= 0.1f) continue
            val dx = obj.gridX - player.x
            val dy = obj.gridY - player.y
            val dist = hypot(dx, dy).coerceAtLeast(0.3f)
            if (dist < 4.5f) {
                val dot = ((dx / dist) * dirX + (dy / dist) * dirY).coerceIn(0f, 1f)
                val sig = obj.emfStrength * (1f - dist / 4.5f) * (0.35f + 0.65f * dot)
                if (sig > peak) peak = sig
            }
        }
        for (ent in entities) {
            if (ent.vanished) continue
            val dx = ent.x - player.x
            val dy = ent.y - player.y
            val dist = hypot(dx, dy).coerceAtLeast(0.4f)
            if (dist < 5.5f) {
                val sig = 0.92f * (1f - dist / 5.5f)
                if (sig > peak) peak = sig
            }
        }
        return peak.coerceIn(0f, 1f)
    }

    fun renderScene(
        drawScope: DrawScope,
        caseDef: CaseDefinition,
        player: Player3DState,
        openDoors: Set<Pair<Int, Int>>,
        entities: List<RuntimeEntity>,
        activeEquipment: EquipmentType?,
        scannerPulseProgress: Float,
        adaptation: AdaptationState,
        collectedEvidenceIds: Set<String>,
        animTimeSec: Float
    ) = with(drawScope) {
        val w = size.width
        val h = size.height
        val theme = caseDef.wallTheme
        val isThermal = activeEquipment == EquipmentType.THERMAL_VISION
        val isSpectral = activeEquipment == EquipmentType.SPECTRAL_LENS
        val isScanner = activeEquipment == EquipmentType.REALITY_SCANNER
        val isMemory = activeEquipment == EquipmentType.MEMORY_RECORDER

        val crouchShift = if (player.isCrouching) h * 0.11f else 0f
        val bobY = sin(player.bobPhase) * (if (player.isRunning) 7f else 3.5f)
        val horizonY = h * 0.5f + player.pitchOffset * h * 0.45f - crouchShift + bobY

        // 1. Ceiling & Floor background gradients
        val ceilTop = if (isThermal) Color(0xFF04071A) else theme.ceilingColor
        val ceilBot = if (isThermal) Color(0xFF0A1438) else theme.ambientFogColor
        drawRect(
            brush = Brush.verticalGradient(listOf(ceilTop, ceilBot), 0f, horizonY.coerceAtLeast(1f)),
            topLeft = Offset.Zero,
            size = Size(w, horizonY.coerceIn(0f, h))
        )

        val floorTop = if (isThermal) Color(0xFF0A1438) else theme.ambientFogColor
        val floorBot = if (isThermal) Color(0xFF13265A) else theme.floorColor
        drawRect(
            brush = Brush.verticalGradient(listOf(floorTop, floorBot), horizonY.coerceIn(0f, h), h),
            topLeft = Offset(0f, horizonY.coerceIn(0f, h)),
            size = Size(w, (h - horizonY).coerceAtLeast(0f))
        )

        // Subtle perspective floor floorboards / tiles
        val floorLines = 8
        for (i in 1..floorLines) {
            val t = (i.toFloat() / floorLines).pow(1.8f)
            val ly = horizonY + (h - horizonY) * t
            if (ly in 0f..h) {
                drawLine(
                    color = Color.White.copy(alpha = 0.04f * t),
                    start = Offset(0f, ly),
                    end = Offset(w, ly),
                    strokeWidth = 1f
                )
            }
        }

        // 2. Raycast Walls & Doors
        val zBuffer = FloatArray(RAY_COUNT) { 30f }
        val sliceW = w / RAY_COUNT

        for (i in 0 until RAY_COUNT) {
            val cameraX = (2f * i / RAY_COUNT) - 1f
            val rayAngle = player.yaw + atan(cameraX * tan(FOV * 0.5f))
            val rayDirX = cos(rayAngle)
            val rayDirY = sin(rayAngle)

            var mapX = floor(player.x).toInt()
            var mapY = floor(player.y).toInt()

            val deltaDistX = if (abs(rayDirX) < 1e-5f) 1e5f else abs(1f / rayDirX)
            val deltaDistY = if (abs(rayDirY) < 1e-5f) 1e5f else abs(1f / rayDirY)

            var stepX: Int
            var sideDistX: Float
            if (rayDirX < 0) {
                stepX = -1
                sideDistX = (player.x - mapX) * deltaDistX
            } else {
                stepX = 1
                sideDistX = (mapX + 1f - player.x) * deltaDistX
            }

            var stepY: Int
            var sideDistY: Float
            if (rayDirY < 0) {
                stepY = -1
                sideDistY = (player.y - mapY) * deltaDistY
            } else {
                stepY = 1
                sideDistY = (mapY + 1f - player.y) * deltaDistY
            }

            var hitTile = '#'
            var side = 0
            var steps = 0

            while (steps < 32) {
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX
                    mapX += stepX
                    side = 0
                } else {
                    sideDistY += deltaDistY
                    mapY += stepY
                    side = 1
                }
                steps++

                if (mapY !in caseDef.mapGrid.indices || mapX !in caseDef.mapGrid[mapY].indices) {
                    hitTile = '#'
                    break
                }
                val tile = caseDef.mapGrid[mapY][mapX]
                if (tile == '#' || tile == 'M') {
                    hitTile = tile
                    break
                }
                if ((tile == 'D' || tile == 'S') && (mapX to mapY) !in openDoors) {
                    hitTile = tile
                    break
                }
            }

            val rawDist = if (side == 0) (sideDistX - deltaDistX) else (sideDistY - deltaDistY)
            val perpDist = (rawDist * cos(rayAngle - player.yaw)).coerceAtLeast(0.18f)
            zBuffer[i] = perpDist

            val lineHeight = (h * 0.85f) / perpDist
            val wallTop = horizonY - lineHeight * 0.5f - crouchShift * 0.35f
            val wallBottom = horizonY + lineHeight * 0.5f - crouchShift * 0.35f

            val hitX = player.x + rayDirX * rawDist
            val hitY = player.y + rayDirY * rawDist
            val texU = if (side == 0) (hitY - floor(hitY)) else (hitX - floor(hitX))

            val fogFactor = (1f - (perpDist / 11f)).coerceIn(0.08f, 1f)
            val sideShade = if (side == 1) 0.78f else 1f
            // Subtle flashlight beam boost near screen center
            val centerFactor = (1f - abs(cameraX) * 1.1f).coerceIn(0f, 1f)
            val flashlightBoost = 1f + centerFactor * 0.32f * fogFactor

            var wallColor = when {
                isThermal -> {
                    when (hitTile) {
                        'M' -> if (adaptation.reflectionsCorrupted) Color(0xFFFF6B35) else Color(0xFF1D4ED8)
                        'S' -> Color(0xFF06B6D4) // Freezing anomalous seam
                        else -> Color(0xFF1E3A8A).lerpTo(Color(0xFF0F172A), 1f - fogFactor)
                    }
                }
                isSpectral -> {
                    when (hitTile) {
                        'S' -> Color(0xFFA855F7)
                        'M' -> Color(0xFF7E22CE)
                        else -> Color(0xFF1E1B4B).lerpTo(Color(0xFF090617), 1f - fogFactor)
                    }
                }
                else -> {
                    when (hitTile) {
                        'D' -> Color(0xFF4A3728)
                        'S' -> if (sin(animTimeSec * 3f + texU * 10f) > 0.6f) Color(0xFF312E81) else theme.trimColor
                        'M' -> Color(0xFF47627A)
                        else -> {
                            // Vertical paneling seam detail
                            if (texU < 0.04f || texU > 0.96f) theme.trimColor else theme.primaryColor
                        }
                    }
                }
            }

            wallColor = Color(
                red = (wallColor.red * sideShade * fogFactor * flashlightBoost).coerceIn(0f, 1f),
                green = (wallColor.green * sideShade * fogFactor * flashlightBoost).coerceIn(0f, 1f),
                blue = (wallColor.blue * sideShade * fogFactor * flashlightBoost).coerceIn(0f, 1f),
                alpha = 1f
            )

            val xPos = i * sliceW
            drawRect(
                color = wallColor,
                topLeft = Offset(xPos, wallTop),
                size = Size(sliceW + 1f, (wallBottom - wallTop).coerceAtLeast(1f))
            )

            // Door handle & frame details
            if (hitTile == 'D' || hitTile == 'S') {
                if (texU in 0.80f..0.88f) {
                    val handleY = wallTop + (wallBottom - wallTop) * 0.54f
                    drawRect(
                        color = if (hitTile == 'S') Color(0xFF22D3EE) else Color(0xFFD97706),
                        topLeft = Offset(xPos, handleY),
                        size = Size(sliceW + 1f, (lineHeight * 0.05f).coerceAtLeast(3f))
                    )
                }
            }

            // Mirror reflective sheen & ghostly smudge if reflections corrupted
            if (hitTile == 'M') {
                val mirrorShimmer = (sin(texU * 14f + animTimeSec * 2f) * 0.5f + 0.5f) * 0.25f * fogFactor
                drawRect(
                    color = (if (adaptation.reflectionsCorrupted) Color(0xFFEF4444) else Color(0xFF22D3EE))
                        .copy(alpha = mirrorShimmer),
                    topLeft = Offset(xPos, wallTop + lineHeight * 0.08f),
                    size = Size(sliceW + 1f, lineHeight * 0.84f)
                )
            }

            // Reality scanner grid pulse sweeping across wall surfaces
            if (isScanner && scannerPulseProgress > 0f) {
                val pulseDist = scannerPulseProgress * 10f
                if (abs(perpDist - pulseDist) < 0.55f) {
                    drawRect(
                        color = Color(0xFF22D3EE).copy(alpha = 0.55f),
                        topLeft = Offset(xPos, wallTop),
                        size = Size(sliceW + 1f, (wallBottom - wallTop).coerceAtLeast(1f))
                    )
                }
            }
        }

        // 3. Depth-sorted 3D Billboards (Interactive Objects + Supernatural Entities)
        data class Billboard(
            val dist: Float,
            val screenX: Float,
            val spriteH: Float,
            val spriteW: Float,
            val drawFn: () -> Unit
        )

        val billboards = mutableListOf<Billboard>()
        val dirX = cos(player.yaw)
        val dirY = sin(player.yaw)
        val planeX = -dirY * tan(FOV * 0.5f)
        val planeY = dirX * tan(FOV * 0.5f)
        val invDet = 1f / (planeX * dirY - dirX * planeY)

        // Interactive objects
        for (obj in caseDef.objects) {
            if (obj.isDoor) continue // Doors rendered in raycaster
            val relX = obj.gridX - player.x
            val relY = obj.gridY - player.y
            val transformX = invDet * (dirY * relX - dirX * relY)
            val transformY = invDet * (-planeY * relX + planeX * relY)
            if (transformY <= 0.2f) continue

            val screenX = (w * 0.5f) * (1f + transformX / transformY)
            val spriteSize = (h * 0.55f) / transformY
            val rayIdx = ((screenX / w) * RAY_COUNT).toInt().coerceIn(0, RAY_COUNT - 1)
            if (transformY > zBuffer[rayIdx] + 0.35f) continue

            val isCollected = obj.linkedEvidenceId != null && obj.linkedEvidenceId in collectedEvidenceIds

            billboards.add(
                Billboard(transformY, screenX, spriteSize, spriteSize * 0.7f) {
                    val baseY = if (obj.requiresCrouch) {
                        horizonY + spriteSize * 0.42f
                    } else {
                        horizonY + spriteSize * 0.08f
                    }
                    val boxW = spriteSize * 0.48f
                    val boxH = spriteSize * 0.42f
                    val topLeft = Offset(screenX - boxW * 0.5f, baseY - boxH * 0.5f)

                    val accentColor = when {
                        isThermal && obj.thermalDescription != null -> Color(0xFFFB7185)
                        isSpectral && obj.spectralDescription != null -> Color(0xFFC084FC)
                        isCollected -> Color(0xFF10B981)
                        obj.kind == ObjectKind.FLOOR_CLUE -> Color(0xFFF59E0B)
                        else -> Color(0xFF22D3EE)
                    }

                    // Render stylized 3D prop silhouette
                    drawRoundRect(
                        color = Color(0xFF0D1421).copy(alpha = 0.85f),
                        topLeft = topLeft,
                        size = Size(boxW, boxH),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = accentColor.copy(alpha = if (isCollected) 0.55f else 0.9f),
                        topLeft = topLeft,
                        size = Size(boxW, boxH),
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = 2.5f)
                    )

                    // Inner icon detail by ObjectKind
                    when (obj.kind) {
                        ObjectKind.CLOCK -> {
                            val cx = screenX
                            val cy = baseY
                            val r = min(boxW, boxH) * 0.32f
                            drawCircle(accentColor, r, Offset(cx, cy), style = Stroke(2f))
                            val tickAngle = -animTimeSec * 1.4f // Backward ticking!
                            drawLine(
                                Color(0xFFEF4444),
                                Offset(cx, cy),
                                Offset(cx + cos(tickAngle) * r * 0.8f, cy + sin(tickAngle) * r * 0.8f),
                                strokeWidth = 2f
                            )
                        }
                        ObjectKind.PHOTOGRAPH, ObjectKind.TV_SCREEN, ObjectKind.COMPUTER -> {
                            drawRect(
                                color = accentColor.copy(alpha = 0.28f),
                                topLeft = Offset(topLeft.x + boxW * 0.16f, topLeft.y + boxH * 0.18f),
                                size = Size(boxW * 0.68f, boxH * 0.54f)
                            )
                        }
                        else -> {
                            drawCircle(
                                color = accentColor,
                                radius = (min(boxW, boxH) * 0.14f).coerceAtLeast(3f),
                                center = Offset(screenX, baseY)
                            )
                        }
                    }

                    // Memory Recorder temporal echo hologram near objects with memoryEchoDescription
                    if (isMemory && obj.memoryEchoDescription != null) {
                        val echoAlpha = (sin(animTimeSec * 4f) * 0.25f + 0.45f)
                        drawOval(
                            color = Color(0xFF22D3EE).copy(alpha = echoAlpha),
                            topLeft = Offset(screenX - boxW * 0.4f, baseY - boxH * 1.2f),
                            size = Size(boxW * 0.8f, boxH * 1.3f),
                            style = Stroke(2f)
                        )
                    }
                }
            )
        }

        // Supernatural Entities
        for (ent in entities) {
            if (ent.vanished) continue
            if (ent.def.visibleOnlyInSpectralOrMirror && !isSpectral) continue

            val relX = ent.x - player.x
            val relY = ent.y - player.y
            val transformX = invDet * (dirY * relX - dirX * relY)
            val transformY = invDet * (-planeY * relX + planeX * relY)
            if (transformY <= 0.35f) continue

            val screenX = (w * 0.5f) * (1f + transformX / transformY)
            val rayIdx = ((screenX / w) * RAY_COUNT).toInt().coerceIn(0, RAY_COUNT - 1)
            if (transformY > zBuffer[rayIdx] + 0.25f) continue

            val entH = (h * 0.78f) / transformY
            val entW = entH * 0.38f

            billboards.add(
                Billboard(transformY, screenX, entH, entW) {
                    val jitterX = if (ent.def.type == EntityType.THE_COPY || ent.def.type == EntityType.THE_SILENT) {
                        sin(animTimeSec * 19f) * 4f
                    } else 0f
                    val ex = screenX + jitterX
                    val topY = horizonY - entH * 0.52f - crouchShift * 0.35f

                    val bodyColor = when {
                        isThermal -> {
                            if (adaptation.thermalMasked) Color(0xFF38BDF8).copy(alpha = 0.45f) // Masked cold!
                            else Color(0xFFFF4500).copy(alpha = 0.9f)
                        }
                        isSpectral -> Color(0xFFC084FC).copy(alpha = 0.82f)
                        ent.def.type == EntityType.THE_SILENT -> Color(0xFF94A3B8).copy(alpha = 0.25f)
                        else -> Color(0xFF05070B).copy(alpha = 0.92f)
                    }

                    // Humanoid silhouette
                    val headR = entW * 0.28f
                    drawCircle(
                        color = bodyColor,
                        radius = headR,
                        center = Offset(ex, topY + headR)
                    )
                    val torsoPath = Path().apply {
                        moveTo(ex - entW * 0.42f, topY + headR * 2.1f)
                        lineTo(ex + entW * 0.42f, topY + headR * 2.1f)
                        lineTo(ex + entW * 0.32f, topY + entH * 0.95f)
                        lineTo(ex - entW * 0.32f, topY + entH * 0.95f)
                        close()
                    }
                    drawPath(torsoPath, color = bodyColor)

                    // Faint glowing eyes in dark/spectral/observer mode
                    val eyeColor = when {
                        isThermal && adaptation.thermalMasked -> Color(0xFF22D3EE)
                        ent.def.type == EntityType.THE_OBSERVER -> Color(0xFFEF4444)
                        else -> Color(0xFF22D3EE)
                    }
                    drawCircle(eyeColor.copy(alpha = 0.85f), (headR * 0.16f).coerceAtLeast(2f), Offset(ex - headR * 0.32f, topY + headR))
                    drawCircle(eyeColor.copy(alpha = 0.85f), (headR * 0.16f).coerceAtLeast(2f), Offset(ex + headR * 0.32f, topY + headR))
                }
            )
        }

        // Draw far-to-near
        billboards.sortedByDescending { it.dist }.forEach { it.drawFn() }

        // 4. Equipment Optical Overlays
        if (isThermal) {
            drawRect(Color(0xFF0EA5E9).copy(alpha = 0.08f))
        } else if (isSpectral) {
            drawRect(Color(0xFF9333EA).copy(alpha = 0.14f))
        }

        // Vignette for psychological suspense
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color(0xAA03050A)),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = max(w, h) * 0.68f
            )
        )
    }

    private fun Color.lerpTo(other: Color, fraction: Float): Color {
        val f = fraction.coerceIn(0f, 1f)
        return Color(
            red = red + (other.red - red) * f,
            green = green + (other.green - green) * f,
            blue = blue + (other.blue - blue) * f,
            alpha = alpha + (other.alpha - alpha) * f
        )
    }
}
