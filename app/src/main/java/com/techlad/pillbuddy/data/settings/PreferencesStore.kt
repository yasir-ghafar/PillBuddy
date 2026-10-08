package com.techlad.pillbuddy.data.settings

import com.techlad.pillbuddy.data.model.AppSettings
import com.techlad.pillbuddy.data.model.TextScale
import kotlinx.coroutines.flow.Flow

/**
 * App settings contract. The Android implementation uses Jetpack DataStore Preferences.
 * An iOS KMM target can implement the same contract with DataStore's multiplatform core or NSUserDefaults.
 */
interface PreferencesStore {
    val settings: Flow<AppSettings>

    suspend fun setTextScale(textScale: TextScale)

    suspend fun setHighContrast(enabled: Boolean)

    suspend fun setHaptics(enabled: Boolean)

    suspend fun setSpokenConfirmation(enabled: Boolean)

    suspend fun isSamplesSeeded(): Boolean

    suspend fun setSamplesSeeded()
}
