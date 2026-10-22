package com.techlad.pillbuddy.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techlad.pillbuddy.notification.DoseReminderPermissions
import kotlinx.coroutines.launch
import com.techlad.pillbuddy.data.AppContainer
import com.techlad.pillbuddy.data.model.AppSettings
import com.techlad.pillbuddy.ui.screens.home.HomeScreen
import com.techlad.pillbuddy.ui.screens.home.HomeViewModel
import com.techlad.pillbuddy.ui.screens.home.HomeViewModelFactory
import com.techlad.pillbuddy.ui.screens.new_reminder.NewPillBuddyScreen
import com.techlad.pillbuddy.ui.screens.new_reminder.NewReminderViewModel
import com.techlad.pillbuddy.ui.screens.new_reminder.NewReminderViewModelFactory
import com.techlad.pillbuddy.ui.screens.OnboardingScreen
import com.techlad.pillbuddy.ui.screens.settings.DoseHistoryScreen
import com.techlad.pillbuddy.ui.screens.settings.SettingsFormScreen
import com.techlad.pillbuddy.ui.theme.PillBuddyBackground

private enum class AppScreen { Onboarding, Home, NewPillBuddy, Settings, DoseHistory }

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
            doseAlarms = container.doseAlarms,
        ),
    )
    val newReminderViewModel: NewReminderViewModel = viewModel(
        factory = NewReminderViewModelFactory(
            reminders = container.reminders,
            doseAlarms = container.doseAlarms,
        ),
    )
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch { container.doseAlarms.rescheduleAll() }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    DoseReminderPermissions(enabled = screen == AppScreen.Home)

    BackHandler(enabled = screen != AppScreen.Onboarding) {
        screen = when (screen) {
            AppScreen.Home -> AppScreen.Onboarding
            AppScreen.DoseHistory -> AppScreen.Settings
            else -> AppScreen.Home
        }
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

            AppScreen.Settings -> SettingsFormScreen(
                preferences = container.preferences,
                onBack = { screen = AppScreen.Home },
                onOpenDoseHistory = { screen = AppScreen.DoseHistory },
                modifier = contentModifier,
            )

            AppScreen.DoseHistory -> DoseHistoryScreen(
                doseHistory = container.doseHistory,
                onBack = { screen = AppScreen.Settings },
                modifier = contentModifier,
            )

            AppScreen.NewPillBuddy -> NewPillBuddyScreen(
                onBack = { screen = AppScreen.Home },
                defaults = settings,
                onCreate = { draft ->
                    newReminderViewModel.createReminder(draft)
                    screen = AppScreen.Home
                },
                modifier = contentModifier
            )
        }
    }
}
