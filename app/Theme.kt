package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Color(0xFF003918),
    primaryContainer = Color(0xFF005225),
    onPrimaryContainer = Color(0xFF6BFF9E),
    secondary = VibrantCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF86F3FF),
    tertiary = ElectricPurple,
    onTertiary = Color.White,
    background = DeepCharcoal,
    onBackground = TextWhite,
    surface = ElevatedCardFill,
    onSurface = TextWhite,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = TextMutedGray,
    outline = SubtleBorder,
    outlineVariant = SurfaceBorderGlow,
    error = AlertRed,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Color(0xFF003918),
    background = DeepCharcoal,
    onBackground = TextWhite,
    surface = ElevatedCardFill,
    onSurface = TextWhite,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = TextMutedGray,
    outline = SubtleBorder,
    error = AlertRed
)

@Composable
fun MinecraftAfkBotTheme(
    darkTheme: Boolean = true, // Default to sleek cyberpunk dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep backwards-compat alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MinecraftAfkBotTheme(darkTheme = true, content = content)
}
