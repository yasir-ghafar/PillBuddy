package com.techlad.pillbuddy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.techlad.pillbuddy.data.model.TextScale

@Composable
fun PillBuddyTheme(
    textScale: TextScale = TextScale.DEFAULT,
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    val palette = if (highContrast) HighContrastPalette else LightPalette
    val density = LocalDensity.current
    val scaledDensity = Density(
        density = density.density,
        fontScale = density.fontScale * textScale.multiplier,
    )
    CompositionLocalProvider(
        LocalPillBuddyColors provides palette,
        LocalDensity provides scaledDensity,
    ) {
        MaterialTheme(
            colorScheme = palette.toColorScheme(),
            typography = Typography,
            content = content,
        )
    }
}
