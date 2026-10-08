package com.techlad.pillbuddy.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class PillBuddyPalette(
    val dark: Color,
    val darker: Color,
    val medium: Color,
    val lighter: Color,
    val light: Color,
    val background: Color,
    val card: Color,
    val onPrimary: Color,
)

val LightPalette = PillBuddyPalette(
    dark = Color(0xFF2E424D),
    darker = Color(0xFF5B8291),
    medium = Color(0xFF98DAD9),
    lighter = Color(0xFFEAEBED),
    light = Color(0xFFF5F5F5),
    background = Color(0xFFE4F0F0),
    card = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
)

val HighContrastPalette = PillBuddyPalette(
    dark = Color.Black,
    darker = Color.Black,
    medium = Color(0xFFFFF4A3),
    lighter = Color.Black,
    light = Color.White,
    background = Color.White,
    card = Color.White,
    onPrimary = Color.White,
)

val LocalPillBuddyColors = staticCompositionLocalOf { LightPalette }

val PillBuddyDark: Color
    @Composable get() = LocalPillBuddyColors.current.dark

val PillBuddyDarker: Color
    @Composable get() = LocalPillBuddyColors.current.darker

val PillBuddyMedium: Color
    @Composable get() = LocalPillBuddyColors.current.medium

val PillBuddyLighter: Color
    @Composable get() = LocalPillBuddyColors.current.lighter

val PillBuddyLight: Color
    @Composable get() = LocalPillBuddyColors.current.light

val PillBuddyBackground: Color
    @Composable get() = LocalPillBuddyColors.current.background

val PillBuddyCard: Color
    @Composable get() = LocalPillBuddyColors.current.card

val PillBuddyOnPrimary: Color
    @Composable get() = LocalPillBuddyColors.current.onPrimary

fun PillBuddyPalette.toColorScheme(): ColorScheme = lightColorScheme(
    primary = dark,
    onPrimary = onPrimary,
    secondary = darker,
    onSecondary = onPrimary,
    tertiary = medium,
    onTertiary = dark,
    background = background,
    onBackground = dark,
    surface = card,
    onSurface = dark,
    surfaceVariant = lighter,
    onSurfaceVariant = darker,
    outline = lighter,
    outlineVariant = medium,
)
