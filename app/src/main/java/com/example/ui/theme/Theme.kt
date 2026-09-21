package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF00222B),
    primaryContainer = Color(0xFF004D5C),
    onPrimaryContainer = Color(0xFFB5F5FF),
    secondary = LaserEmerald,
    onSecondary = Color(0xFF003820),
    secondaryContainer = Color(0xFF005230),
    onSecondaryContainer = Color(0xFF86FFCA),
    tertiary = ElectricBlue,
    onTertiary = Color(0xFF00344F),
    tertiaryContainer = Color(0xFF004C72),
    onTertiaryContainer = Color(0xFFCBE6FF),
    error = CyberCrimson,
    onError = Color(0xFF450013),
    errorContainer = CyberCrimsonDim,
    onErrorContainer = Color(0xFFFFD9DF),
    background = GunmetalBackground,
    onBackground = TextSilver,
    surface = SteelSurface,
    onSurface = TextSilver,
    surfaceVariant = SteelSurfaceVariant,
    onSurfaceVariant = TextSteelSecondary,
    outline = SteelBorder,
    outlineVariant = SteelBorderHighlight,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek metallic dark theme
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
