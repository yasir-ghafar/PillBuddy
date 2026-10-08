package com.techlad.pillbuddy.data

import android.content.Context
import com.techlad.pillbuddy.data.local.openPillBuddyDatabase
import com.techlad.pillbuddy.data.repository.DoseHistoryRepository
import com.techlad.pillbuddy.data.repository.ReminderRepository
import com.techlad.pillbuddy.data.settings.DataStorePreferencesStore
import com.techlad.pillbuddy.data.settings.PreferencesStore
import kotlinx.coroutines.Dispatchers

/**
 * Android graph for storage. Repositories depend on the generated SQLDelight database and a
 * dispatcher, so they can move into a KMM common module with the `.sq` schema. [preferences]
 * is the Jetpack DataStore implementation of [PreferencesStore].
 */
class AppContainer(context: Context) {
    private val database = openPillBuddyDatabase(context)
    private val io = Dispatchers.IO

    val reminders = ReminderRepository(database, io)
    val doseHistory = DoseHistoryRepository(database, io)
    val preferences: PreferencesStore = DataStorePreferencesStore(context)
}
