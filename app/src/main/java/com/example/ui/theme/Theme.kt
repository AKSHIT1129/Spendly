package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = MintAccent,
    secondary = IndigoAccent,
    onSecondary = Color.White,
    background = RawSlateBg,
    onBackground = RawLightText,
    surface = RawSlateSurface,
    onSurface = RawLightText,
    surfaceVariant = RawSlateCard,
    onSurfaceVariant = RawLightText,
    error = CoralRed,
    onError = Color.White,
    outline = RawSlateLine
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = IndigoAccent,
    onSecondary = Color.White,
    background = RawLightBg,
    onBackground = RawDarkText,
    surface = RawLightSurface,
    onSurface = RawDarkText,
    surfaceVariant = RawLightCard,
    onSurfaceVariant = RawDarkGrayText,
    error = CoralRed,
    onError = Color.White,
    outline = RawLightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
