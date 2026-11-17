package com.techlad.pillbuddy.ui.screens.new_reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.techlad.pillbuddy.data.model.NewReminder
import com.techlad.pillbuddy.data.model.Reminder
import com.techlad.pillbuddy.data.repository.ReminderStore
import com.techlad.pillbuddy.notification.DoseAlarmScheduler

interface SavedReminderScheduler {
    suspend fun schedule(reminder: Reminder, fireImmediatelyIfLeadPassed: Boolean)
    fun cancel(reminderId: Long)
}

class NewReminderViewModel(
    private val reminders: ReminderStore,
    private val scheduler: SavedReminderScheduler,
) : ViewModel() {
    suspend fun createReminder(draft: NewReminder): Boolean {
        return try {
            val id = reminders.insert(draft)
            reminders.getById(id)?.let { saved ->
                scheduler.schedule(saved, fireImmediatelyIfLeadPassed = true)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun updateReminder(id: Long, draft: NewReminder): Boolean {
        return try {
            val existing = reminders.getById(id) ?: return false
            val updated = existing.withDraft(draft)
            reminders.update(updated)
            scheduler.cancel(id)
            scheduler.schedule(updated, fireImmediatelyIfLeadPassed = true)
            true
        } catch (_: Exception) {
            false
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

            override fun cancel(reminderId: Long) {
                doseAlarms.cancel(reminderId)
            }
        }
        return NewReminderViewModel(reminders, scheduler) as T
    }
}
