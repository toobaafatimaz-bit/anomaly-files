package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.DetectiveRank
import com.example.ui.theme.*

@Composable
fun DossierScanlineBackground(
    modifier: Modifier = Modifier,
    accentGlow: Color = ScannerCyan,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AbyssalNavy, DossierSlate, AbyssalNavy)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Subtle atmospheric radial glow top-right
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentGlow.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.12f),
                    radius = size.width * 0.65f
                )
            )
            // Subtle CRT scanlines
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color.White.copy(alpha = 0.018f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += 8f
            }
        }
        content()
    }
}

@Composable
fun DossierTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            if (subtitle != null) {
                Text(
                    text = subtitle.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
    }
}

@Composable
fun DossierPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun ClassifiedBadge(
    text: String,
    color: Color = EmergencyRed,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(1.dp, color.copy(alpha = 0.8f), RoundedCornerShape(3.dp))
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(3.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RankStatusChip(
    xp: Int,
    credits: Int,
    modifier: Modifier = Modifier
) {
    val rank = DetectiveRank.fromXp(xp)
    Row(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
            .background(PanelCharcoal, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ClassifiedBadge(text = rank.clearanceCode, color = ScannerCyan)
        Text(
            text = rank.title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "•  $credits CR",
            style = MaterialTheme.typography.labelLarge,
            color = ClassifiedAmber
        )
    }
}
