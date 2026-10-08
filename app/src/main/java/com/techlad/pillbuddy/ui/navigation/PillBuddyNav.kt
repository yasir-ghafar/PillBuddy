package com.techlad.pillbuddy.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techlad.pillbuddy.data.AppContainer
import com.techlad.pillbuddy.data.model.AppSettings
import com.techlad.pillbuddy.ui.screens.home.HomeScreen
import com.techlad.pillbuddy.ui.screens.home.HomeViewModel
import com.techlad.pillbuddy.ui.screens.home.HomeViewModelFactory
import com.techlad.pillbuddy.ui.screens.NewPillBuddyScreen
import com.techlad.pillbuddy.ui.screens.OnboardingScreen
import com.techlad.pillbuddy.ui.screens.settings.SettingsFormScreen
import com.techlad.pillbuddy.ui.screens.settings.SettingsScreen
import com.techlad.pillbuddy.ui.theme.PillBuddyBackground

private enum class AppScreen { Onboarding, Home, NewPillBuddy, Settings }

@Composable
fun PillBuddyApp(
    container: AppContainer,
    settings: AppSettings,
) {
    var screen by rememberSaveable { mutableStateOf(AppScreen.Onboarding) }
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            reminders = container.reminders,
            doseHistory = container.doseHistory,
            preferences = container.preferences,
        ),
    )

    BackHandler(enabled = screen != AppScreen.Onboarding) {
        screen = if (screen == AppScreen.Home) AppScreen.Onboarding else AppScreen.Home
    }

    Scaffold(
        containerColor = PillBuddyBackground
    ) { innerPadding ->
        val contentModifier = Modifier
            .padding(innerPadding)
            .background(PillBuddyBackground)
        when (screen) {
            AppScreen.Onboarding -> OnboardingScreen(
                onContinue = { screen = AppScreen.Home },
                modifier = contentModifier
            )

            AppScreen.Home -> HomeScreen(
                onAdd = { screen = AppScreen.NewPillBuddy },
                onOpenSettings = { screen = AppScreen.Settings },
                settings = settings,
                viewModel = homeViewModel,
                modifier = contentModifier
            )

            /*AppScreen.Settings -> SettingsScreen(
                preferences = container.preferences,
                doseHistory = container.doseHistory,
                onBack = { screen = AppScreen.Home },
                modifier = contentModifier
            )*/

            AppScreen.Settings -> SettingsFormScreen(
                onBack = { screen = AppScreen.Home },
                modifier = contentModifier
            )

            AppScreen.NewPillBuddy -> NewPillBuddyScreen(
                onBack = { screen = AppScreen.Home },
                onCreate = { draft ->
                    homeViewModel.addReminder(draft)
                    screen = AppScreen.Home
                },
                modifier = contentModifier
            )
        }
    }
}
