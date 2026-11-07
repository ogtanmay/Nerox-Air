package com.nexamusic.air.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF78A8FF),
    secondary = Color(0xFF9CC7FF),
    surface = Color(0xFF12161E),
    background = Color(0xFF0D1117)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF2D67C9),
    secondary = Color(0xFF4B89D8),
    surface = Color(0xFFF5F8FF),
    background = Color(0xFFEFF3FF)
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
