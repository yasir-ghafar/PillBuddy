package com.techlad.pillbuddy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PillBuddyDark,
    onPrimary = PillBuddyOnPrimary,
    secondary = PillBuddyDarker,
    onSecondary = PillBuddyOnPrimary,
    tertiary = PillBuddyMedium,
    onTertiary = PillBuddyDark,
    background = PillBuddyBackground,
    onBackground = PillBuddyDark,
    surface = PillBuddyCard,
    onSurface = PillBuddyDark,
    surfaceVariant = PillBuddyLighter,
    onSurfaceVariant = PillBuddyDarker,
    outline = PillBuddyLighter,
    outlineVariant = PillBuddyMedium
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
