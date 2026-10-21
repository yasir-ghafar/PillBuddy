package com.techlad.pillbuddy.ui.screens.new_reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.techlad.pillbuddy.data.model.NewReminder
import com.techlad.pillbuddy.data.repository.ReminderRepository
import com.techlad.pillbuddy.notification.DoseAlarmScheduler
import kotlinx.coroutines.launch

class NewReminderViewModel(
    private val reminders: ReminderRepository,
    private val doseAlarms: DoseAlarmScheduler,
) : ViewModel() {
    fun createReminder(draft: NewReminder) {
        viewModelScope.launch {
            val id = reminders.insert(draft)
            reminders.getById(id)?.let { saved ->
                doseAlarms.scheduleReminder(saved, fireImmediatelyIfLeadPassed = true)
            }
        }
    }
}

class NewReminderViewModelFactory(
    private val reminders: ReminderRepository,
    private val doseAlarms: DoseAlarmScheduler,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewReminderViewModel(reminders, doseAlarms) as T
    }
}
