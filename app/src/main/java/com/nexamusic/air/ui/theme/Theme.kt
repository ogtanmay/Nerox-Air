package com.nexamusic.air.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF2D55),
    secondary = Color(0xFF64D2FF),
    surface = Color(0xFF090A0C),
    surfaceVariant = Color(0xFF202125),
    background = Color(0xFF000000)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFFE91E4D),
    secondary = Color(0xFF007EA7),
    surface = Color(0xFFF7F7F8),
    background = Color(0xFFFFFFFF)
)

@Composable
fun NexaMusicAirTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
