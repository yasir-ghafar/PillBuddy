package com.techlad.pillbuddy.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.techlad.pillbuddy.data.model.AppSettings
import com.techlad.pillbuddy.data.model.TextScale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_settings",
)

class DataStorePreferencesStore(
    context: Context,
) : PreferencesStore {
    private val dataStore = context.applicationContext.userPreferencesDataStore

    override val settings: Flow<AppSettings> = dataStore.data
        .map { preferences -> preferences.toAppSettings() }
        .catch { error ->
            if (error is IOException) emit(AppSettings()) else throw error
        }

    override suspend fun setTextScale(textScale: TextScale) {
        dataStore.edit { preferences ->
            preferences[Keys.TEXT_SCALE] = textScale.name
        }
    }

    override suspend fun setHighContrast(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.HIGH_CONTRAST] = enabled
        }
    }

    override suspend fun setHaptics(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.HAPTICS] = enabled
        }
    }

    override suspend fun setSpokenConfirmation(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.SPOKEN_CONFIRMATION] = enabled
        }
    }

    override suspend fun isSamplesSeeded(): Boolean {
        return dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { preferences -> preferences[Keys.SAMPLES_SEEDED] ?: false }
            .first()
    }

    override suspend fun setSamplesSeeded() {
        dataStore.edit { preferences ->
            preferences[Keys.SAMPLES_SEEDED] = true
        }
    }

    private fun Preferences.toAppSettings(): AppSettings {
        val scale = this[Keys.TEXT_SCALE]
            ?.let { name -> TextScale.entries.firstOrNull { it.name == name } }
            ?: TextScale.DEFAULT
        return AppSettings(
            textScale = scale,
            highContrast = this[Keys.HIGH_CONTRAST] ?: false,
            haptics = this[Keys.HAPTICS] ?: true,
            spokenConfirmation = this[Keys.SPOKEN_CONFIRMATION] ?: false,
        )
    }

    private object Keys {
        val TEXT_SCALE = stringPreferencesKey("text_scale")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val HAPTICS = booleanPreferencesKey("haptics")
        val SPOKEN_CONFIRMATION = booleanPreferencesKey("spoken_confirmation")
        val SAMPLES_SEEDED = booleanPreferencesKey("samples_seeded")
    }
}
