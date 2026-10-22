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

    /** Latest saved settings, including defaults that have not been collected yet. */
    suspend fun current(): AppSettings

    /**
     * Writes the profile and reminder defaults. Text scale, contrast, haptics, and
     * spoken confirmation stay as they are.
     */
    suspend fun saveDefaults(settings: AppSettings)

    suspend fun isSamplesSeeded(): Boolean

    suspend fun clearSamplesSeeded()
}
