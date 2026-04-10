package com.exist.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkExistColorScheme = darkColorScheme(
    primary = ExistAccent,
    onPrimary = ExistBackground,
    background = ExistBackground,
    onBackground = ExistPrimary,
    surface = ExistSurface,
    onSurface = ExistPrimary,
    surfaceContainer = ExistContainer,
    tertiary = ExistMuted
)

private val LightExistColorScheme = lightColorScheme(
    primary = ExistAccent,
    onPrimary = ExistPrimary,
    background = ExistPrimary,
    onBackground = ExistBackground,
    surface = LightSurface,
    onSurface = ExistBackground,
    surfaceContainer = Color(0xFFDDE8D4),
    tertiary = ExistMuted
)

@Composable
fun ExistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkExistColorScheme else LightExistColorScheme,
        typography = ExistTypography,
        content = content
    )
}
