package com.techlad.pillbuddy.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.techlad.pillbuddy.data.model.Frequency
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

    override suspend fun current(): AppSettings = readSettings()

    override suspend fun saveDefaults(settings: AppSettings) {
        val name = settings.firstName.trim().ifEmpty { AppSettings().firstName }
        val frequency = settings.defaultFrequency
        val cycle = if (settings.hourCycle == 24) 24 else 12
        dataStore.edit { preferences ->
            preferences[Keys.FIRST_NAME] = name
            preferences[Keys.DEFAULT_FREQUENCY] = frequency.name
            preferences[Keys.DEFAULT_HOUR] = settings.defaultHour.coerceIn(0, 23)
            preferences[Keys.DEFAULT_MINUTE] = settings.defaultMinute.coerceIn(0, 59)
            preferences[Keys.HOUR_CYCLE] = cycle
            preferences[Keys.NOTIFY_MINUTES] = settings.notifyMinutesBefore.coerceAtLeast(0)
            preferences[Keys.SNOOZE_MINUTES] = settings.snoozeMinutes.coerceAtLeast(0)
            preferences[Keys.REFILL_DAYS] = settings.refillDaysBefore.coerceAtLeast(0)
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

    private suspend fun readSettings(): AppSettings {
        return dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { preferences -> preferences.toAppSettings() }
            .first()
    }

    override suspend fun clearSamplesSeeded() {
        dataStore.edit { preferences ->
            preferences[Keys.SAMPLES_SEEDED] = false
        }
    }

    private fun Preferences.toAppSettings(): AppSettings {
        val scale = this[Keys.TEXT_SCALE]
            ?.let { name -> TextScale.entries.firstOrNull { it.name == name } }
            ?: TextScale.DEFAULT
        val frequency = this[Keys.DEFAULT_FREQUENCY]
            ?.let { name -> Frequency.entries.firstOrNull { it.name == name } }
            ?: Frequency.DAY
        val defaults = AppSettings()
        return AppSettings(
            textScale = scale,
            highContrast = this[Keys.HIGH_CONTRAST] ?: false,
            haptics = this[Keys.HAPTICS] ?: true,
            spokenConfirmation = this[Keys.SPOKEN_CONFIRMATION] ?: false,
            firstName = this[Keys.FIRST_NAME]?.trim()?.ifEmpty { null } ?: defaults.firstName,
            defaultFrequency = frequency,
            defaultHour = (this[Keys.DEFAULT_HOUR] ?: defaults.defaultHour).coerceIn(0, 23),
            defaultMinute = (this[Keys.DEFAULT_MINUTE] ?: defaults.defaultMinute).coerceIn(0, 59),
            hourCycle = if (this[Keys.HOUR_CYCLE] == 24) 24 else 12,
            notifyMinutesBefore = (this[Keys.NOTIFY_MINUTES] ?: defaults.notifyMinutesBefore).coerceAtLeast(0),
            snoozeMinutes = (this[Keys.SNOOZE_MINUTES] ?: defaults.snoozeMinutes).coerceAtLeast(0),
            refillDaysBefore = (this[Keys.REFILL_DAYS] ?: defaults.refillDaysBefore).coerceAtLeast(0),
        )
    }

    private object Keys {
        val TEXT_SCALE = stringPreferencesKey("text_scale")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val HAPTICS = booleanPreferencesKey("haptics")
        val SPOKEN_CONFIRMATION = booleanPreferencesKey("spoken_confirmation")
        val SAMPLES_SEEDED = booleanPreferencesKey("samples_seeded")
        val FIRST_NAME = stringPreferencesKey("first_name")
        val DEFAULT_FREQUENCY = stringPreferencesKey("default_frequency")
        val DEFAULT_HOUR = intPreferencesKey("default_hour")
        val DEFAULT_MINUTE = intPreferencesKey("default_minute")
        val HOUR_CYCLE = intPreferencesKey("hour_cycle")
        val NOTIFY_MINUTES = intPreferencesKey("notify_minutes_before")
        val SNOOZE_MINUTES = intPreferencesKey("snooze_minutes")
        val REFILL_DAYS = intPreferencesKey("refill_days_before")
    }
}
