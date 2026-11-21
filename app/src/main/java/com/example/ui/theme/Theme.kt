package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AnomalyColorScheme = darkColorScheme(
    primary = ScannerCyan,
    onPrimary = AbyssalNavy,
    primaryContainer = ScannerCyanDim,
    onPrimaryContainer = DossierTextPrimary,
    secondary = SpectralViolet,
    onSecondary = AbyssalNavy,
    secondaryContainer = ElevatedSlate,
    onSecondaryContainer = SpectralVioletBright,
    tertiary = ClassifiedAmber,
    onTertiary = AbyssalNavy,
    error = EmergencyRed,
    onError = DossierTextPrimary,
    errorContainer = EmergencyRedDim,
    onErrorContainer = DossierTextPrimary,
    background = AbyssalNavy,
    onBackground = DossierTextPrimary,
    surface = DossierSlate,
    onSurface = DossierTextPrimary,
    surfaceVariant = PanelCharcoal,
    onSurfaceVariant = DossierTextMuted,
    outline = BorderSlate,
    outlineVariant = ElevatedSlate
)

val AnomalyShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

@Composable
fun AnomalyFilesTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AnomalyColorScheme,
        typography = AnomalyTypography,
        shapes = AnomalyShapes,
        content = content
    )
}
