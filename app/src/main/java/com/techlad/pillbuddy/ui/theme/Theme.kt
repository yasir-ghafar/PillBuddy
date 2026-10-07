package com.techlad.pillbuddy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ReMedDark,
    onPrimary = ReMedOnPrimary,
    secondary = ReMedDarker,
    onSecondary = ReMedOnPrimary,
    tertiary = ReMedMedium,
    onTertiary = ReMedDark,
    background = ReMedBackground,
    onBackground = ReMedDark,
    surface = ReMedCard,
    onSurface = ReMedDark,
    surfaceVariant = ReMedLighter,
    onSurfaceVariant = ReMedDarker,
    outline = ReMedLighter,
    outlineVariant = ReMedMedium
)

@Composable
fun PillBuddyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
