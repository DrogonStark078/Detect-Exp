package com.smarttracker.app.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PureBlack = Color(0xFF000000)
val DarkSurface = Color(0xFF121418)
val DarkSurfaceVariant = Color(0xFF1E222B)
val RadarGreen = Color(0xFF00E676)
val OffWhite = Color(0xFFECEFF1)
val AccentRed = Color(0xFFFF5252)

private val DarkColorScheme = darkColorScheme(
    primary = RadarGreen,
    onPrimary = PureBlack,
    surface = DarkSurface,
    onSurface = OffWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB0BEC5),
    background = PureBlack,
    error = AccentRed
)

@Composable
fun DetExTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
