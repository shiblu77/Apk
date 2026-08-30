package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF001A24),
    primaryContainer = Color(0xFF00384D),
    onPrimaryContainer = NeonCyan,
    secondary = NeonPink,
    onSecondary = Color(0xFF2B0010),
    secondaryContainer = Color(0xFF520023),
    onSecondaryContainer = NeonPink,
    tertiary = NeonGreen,
    onTertiary = Color(0xFF00221B),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    error = NeonRed,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
