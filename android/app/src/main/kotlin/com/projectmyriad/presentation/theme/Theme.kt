package com.projectmyriad.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

// Project Myriad Brand Colors
private val MyriadPrimary = Color(0xFF6366F1) // Indigo
private val MyriadSecondary = Color(0xFF8B5CF6) // Violet
private val MyriadTertiary = Color(0xFF06B6D4) // Cyan

// Light Theme Colors
private val LightColorScheme = lightColorScheme(
    primary = MyriadPrimary,
    secondary = MyriadSecondary,
    tertiary = MyriadTertiary,
    background = Color(0xFFFEFEFE),
    surface = Color(0xFFFEFEFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

// Dark Theme Colors
private val DarkColorScheme = darkColorScheme(
    primary = MyriadPrimary,
    secondary = MyriadSecondary,
    tertiary = MyriadTertiary,
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
)

/**
 * Project Myriad Material 3 Theme
 * Supports both light and dark modes with brand colors.
 */
@Composable
fun ProjectMyriadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}