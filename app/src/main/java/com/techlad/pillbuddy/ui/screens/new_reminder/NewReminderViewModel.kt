package com.techlad.pillbuddy.ui.screens.new_reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.techlad.pillbuddy.data.model.NewReminder
import com.techlad.pillbuddy.data.model.Reminder
import com.techlad.pillbuddy.data.repository.ReminderStore
import com.techlad.pillbuddy.notification.DoseAlarmScheduler
import kotlinx.coroutines.launch

interface SavedReminderScheduler {
    suspend fun schedule(reminder: Reminder, fireImmediatelyIfLeadPassed: Boolean)
}

class NewReminderViewModel(
    private val reminders: ReminderStore,
    private val scheduler: SavedReminderScheduler,
) : ViewModel() {
    fun createReminder(draft: NewReminder) {
        viewModelScope.launch {
            val id = reminders.insert(draft)
            reminders.getById(id)?.let { saved ->
                scheduler.schedule(saved, fireImmediatelyIfLeadPassed = true)
            }
        }
    }
}

class NewReminderViewModelFactory(
    private val reminders: ReminderStore,
    private val doseAlarms: DoseAlarmScheduler,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val scheduler = object : SavedReminderScheduler {
            override suspend fun schedule(reminder: Reminder, fireImmediatelyIfLeadPassed: Boolean) {
                doseAlarms.scheduleReminder(
                    reminder,
                    fireImmediatelyIfLeadPassed = fireImmediatelyIfLeadPassed,
                )
            }
        }
        return NewReminderViewModel(reminders, scheduler) as T
    }
}
