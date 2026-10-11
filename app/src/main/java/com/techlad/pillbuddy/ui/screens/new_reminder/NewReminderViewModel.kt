package com.techlad.pillbuddy.ui.screens.new_reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.techlad.pillbuddy.data.model.NewReminder
import com.techlad.pillbuddy.data.repository.ReminderRepository
import kotlinx.coroutines.launch

class NewReminderViewModel(
    private val reminders: ReminderRepository,
) : ViewModel() {
    fun createReminder(draft: NewReminder) {
        viewModelScope.launch {
            reminders.insert(draft)
        }
    }
}

class NewReminderViewModelFactory(
    private val reminders: ReminderRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewReminderViewModel(reminders) as T
    }
}
