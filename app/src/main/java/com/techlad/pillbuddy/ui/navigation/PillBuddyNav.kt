package com.techlad.pillbuddy.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.techlad.pillbuddy.data.model.sampleRemeds
import com.techlad.pillbuddy.ui.screens.HomeScreen
import com.techlad.pillbuddy.ui.screens.NewPillBuddyScreen
import com.techlad.pillbuddy.ui.screens.OnboardingScreen
import com.techlad.pillbuddy.ui.theme.ReMedBackground

private enum class AppScreen { Onboarding, Home, NewPillBuddy }

@Composable
fun PillBuddyApp() {
    var screen by rememberSaveable { mutableStateOf(AppScreen.Onboarding) }
    var meds by remember { mutableStateOf(sampleRemeds()) }
    var nextId by remember { mutableIntStateOf(4) }

    BackHandler(enabled = screen != AppScreen.Onboarding) {
        screen = if (screen == AppScreen.NewPillBuddy) AppScreen.Home else AppScreen.Onboarding
    }

    Scaffold(
        containerColor = ReMedBackground
    ) { innerPadding ->
        val contentModifier = Modifier
            .padding(innerPadding)
            .background(ReMedBackground)
        when (screen) {
            AppScreen.Onboarding -> OnboardingScreen(
                onContinue = { screen = AppScreen.Home },
                modifier = contentModifier
            )

            AppScreen.Home -> HomeScreen(
                meds = meds,
                onMedsChange = { meds = it },
                onAdd = { screen = AppScreen.NewPillBuddy },
                modifier = contentModifier
            )

            AppScreen.NewPillBuddy -> NewPillBuddyScreen(
                onBack = { screen = AppScreen.Home },
                onCreate = { draft ->
                    meds = meds + draft.copy(id = nextId)
                    nextId += 1
                    screen = AppScreen.Home
                },
                modifier = contentModifier
            )
        }
    }
}
