package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CalmTealPrimaryDark,
    onPrimary = CalmTealOnPrimaryDark,
    primaryContainer = CalmTealContainerDark,
    onPrimaryContainer = CalmTealOnContainerDark,
    secondary = CalmMintSecondaryDark,
    onSecondary = CalmTealOnPrimaryDark,
    secondaryContainer = CalmMintContainerDark,
    onSecondaryContainer = CalmMintSecondaryDark,
    tertiary = CalmSlateTertiaryDark,
    tertiaryContainer = CalmSlateContainerDark,
    background = CalmBackgroundDark,
    surface = CalmSurfaceDark,
    surfaceVariant = CalmSurfaceVariantDark,
    outline = CalmOutlineDark,
    onBackground = CalmOnSurfaceDark,
    onSurface = CalmOnSurfaceDark,
    onSurfaceVariant = CalmOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = CalmTealPrimary,
    onPrimary = CalmTealOnPrimary,
    primaryContainer = CalmTealContainer,
    onPrimaryContainer = CalmTealOnContainer,
    secondary = CalmMintSecondary,
    onSecondary = CalmTealOnPrimary,
    secondaryContainer = CalmMintContainer,
    onSecondaryContainer = CalmMintOnContainer,
    tertiary = CalmSlateTertiary,
    tertiaryContainer = CalmSlateContainer,
    onTertiaryContainer = CalmSlateOnContainer,
    background = CalmBackgroundLight,
    surface = CalmSurfaceLight,
    surfaceVariant = CalmSurfaceVariantLight,
    outline = CalmOutlineLight,
    onBackground = CalmOnSurfaceLight,
    onSurface = CalmOnSurfaceLight,
    onSurfaceVariant = CalmOnSurfaceLight
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
