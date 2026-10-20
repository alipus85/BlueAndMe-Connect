package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AutomotiveColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = AutomotiveBlack,
    primaryContainer = CyanAccentDim,
    onPrimaryContainer = TextPrimary,
    secondary = EcoEmerald,
    onSecondary = AutomotiveBlack,
    secondaryContainer = EcoEmeraldDim,
    onSecondaryContainer = TextPrimary,
    tertiary = WarningAmber,
    onTertiary = AutomotiveBlack,
    background = AutomotiveBlack,
    onBackground = TextPrimary,
    surface = AutomotiveSurface,
    onSurface = TextPrimary,
    surfaceVariant = AutomotiveCard,
    onSurfaceVariant = TextSecondary,
    outline = AutomotiveCardBorder,
    error = DangerRed,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Disabilitato per preservare il cockpit automotive
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AutomotiveColorScheme,
        typography = Typography,
        content = content
    )
}
