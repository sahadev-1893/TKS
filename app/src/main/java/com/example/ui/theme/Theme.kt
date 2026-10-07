package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ChaiPrimaryDarkTheme,
    onPrimary = ChaiBackgroundDark,
    primaryContainer = ChaiPrimaryDark,
    onPrimaryContainer = ChaiTextDark,
    secondary = ChaiSecondaryDarkTheme,
    onSecondary = ChaiBackgroundDark,
    background = ChaiBackgroundDark,
    onBackground = ChaiTextDark,
    surface = ChaiSurfaceDark,
    onSurface = ChaiTextDark,
    surfaceVariant = ChaiSurfaceDark,
    onSurfaceVariant = ChaiTextDark,
    outline = ChaiPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = ChaiPrimary,
    onPrimary = ChaiSurfaceLight,
    primaryContainer = ChaiCardWarm,
    onPrimaryContainer = ChaiPrimaryDark,
    secondary = ChaiSecondary,
    onSecondary = ChaiSurfaceLight,
    secondaryContainer = PendingOrangeLight,
    onSecondaryContainer = ChaiSecondary,
    tertiary = ChaiTertiary,
    background = ChaiBackgroundLight,
    onBackground = ChaiTextPrimary,
    surface = ChaiSurfaceLight,
    onSurface = ChaiTextPrimary,
    surfaceVariant = ChaiCardWarm,
    onSurfaceVariant = ChaiTextSecondary,
    outline = ChaiBorder
)

@Composable
fun TunaKakaTheme(
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
