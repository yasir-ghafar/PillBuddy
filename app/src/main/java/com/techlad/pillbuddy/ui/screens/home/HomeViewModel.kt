package com.techlad.pillbuddy.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.techlad.pillbuddy.data.model.DoseStatus
import com.techlad.pillbuddy.data.model.EpochDays
import com.techlad.pillbuddy.data.model.NewReminder
import com.techlad.pillbuddy.data.model.Remed
import com.techlad.pillbuddy.data.model.ScheduledDose
import com.techlad.pillbuddy.data.model.scheduledDoses
import com.techlad.pillbuddy.data.repository.DoseHistoryRepository
import com.techlad.pillbuddy.data.repository.MissedDose
import com.techlad.pillbuddy.data.repository.ReminderRepository
import com.techlad.pillbuddy.data.settings.PreferencesStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HomeUiState(
    val current: Remed? = null,
    val missed: List<Remed> = emptyList(),
    val upcoming: List<Remed> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val dateLabel: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val reminders: ReminderRepository,
    private val doseHistory: DoseHistoryRepository,
    private val preferences: PreferencesStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    private val now = MutableStateFlow(System.currentTimeMillis())
    private var currentDoses: Map<Long, ScheduledDose> = emptyMap()

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(CLOCK_TICK_MS)
                now.value = System.currentTimeMillis()
            }
        }
        viewModelScope.launch {
            if (!preferences.isSamplesSeeded()) {
                reminders.insertSamples()
                preferences.setSamplesSeeded()
            }
            combine(
                reminders.observeAll(),
                now.map { EpochDays.fromMillis(it) }.distinctUntilChanged().flatMapLatest { day ->
                    doseHistory.observeForDay(day)
                },
                now,
            ) { storedReminders, events, nowMillis ->
                Triple(storedReminders, events, nowMillis)
            }.collect { (storedReminders, events, nowMillis) ->
                val doses = scheduledDoses(storedReminders, events, nowMillis)
                currentDoses = doses.associateBy { it.remed.id }
                val ui = doses.map { it.remed }.toHomeUiState(nowMillis)
                _uiState.value = ui
                val missedIds = ui.missed.map { it.id }.toSet()
                val toRecord = doses.mapNotNull { dose ->
                    if (dose.remed.id !in missedIds) return@mapNotNull null
                    val alreadyLogged = events.any { event ->
                        event.reminderId == dose.remed.id &&
                            (event.status == DoseStatus.MISSED || event.status == DoseStatus.TAKEN)
                    }
                    if (alreadyLogged) return@mapNotNull null
                    MissedDose(
                        reminderId = dose.remed.id,
                        epochDay = dose.epochDay,
                        scheduledMinutes = dose.scheduledMinutes,
                    )
                }
                doseHistory.recordMissed(toRecord, nowMillis)
            }
        }
    }

    fun refresh() {
        now.value = System.currentTimeMillis()
    }

    fun markDone(id: Long) {
        val dose = currentDoses[id] ?: return
        viewModelScope.launch {
            doseHistory.record(
                reminderId = id,
                epochDay = dose.epochDay,
                status = DoseStatus.TAKEN,
                scheduledMinutes = dose.scheduledMinutes,
                actedAtMillis = System.currentTimeMillis(),
                snoozeUntilMillis = null,
            )
        }
    }

    fun snooze(id: Long) {
        val dose = currentDoses[id] ?: return
        val actedAt = System.currentTimeMillis()
        viewModelScope.launch {
            doseHistory.record(
                reminderId = id,
                epochDay = dose.epochDay,
                status = DoseStatus.SNOOZED,
                scheduledMinutes = dose.scheduledMinutes,
                actedAtMillis = actedAt,
                snoozeUntilMillis = actedAt + SNOOZE_MINUTES * 60_000L,
            )
        }
    }

    fun addReminder(draft: NewReminder) {
        viewModelScope.launch {
            reminders.insert(draft)
        }
    }

    private companion object {
        const val CLOCK_TICK_MS = 30_000L
        const val SNOOZE_MINUTES = 10
    }
}

class HomeViewModelFactory(
    private val reminders: ReminderRepository,
    private val doseHistory: DoseHistoryRepository,
    private val preferences: PreferencesStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(reminders, doseHistory, preferences) as T
    }
}

internal fun List<Remed>.toHomeUiState(nowMillis: Long): HomeUiState {
    val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
    val nowMinutes = minutesOfDay(now)
    val open = filter { !it.completed }
    val current = selectLatestReminder(open, now)
    val others = open.filter { it.id != current?.id }
    val missed = others
        .filter { isPast(it, nowMinutes) }
        .sortedWith(reminderTimeOrder)
    val upcoming = others
        .filter { !isPast(it, nowMinutes) }
        .sortedWith(reminderTimeOrder)
    return HomeUiState(
        current = current?.copy(dueNow = isDue(current, now)),
        missed = missed,
        upcoming = upcoming,
        completedCount = count { it.completed },
        totalCount = size,
        dateLabel = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(now.time),
    )
}

internal fun selectLatestReminder(open: List<Remed>, now: Calendar): Remed? {
    if (open.isEmpty()) return null
    val nowMinutes = minutesOfDay(now)
    val scheduled = open.map { reminder -> reminder to minutesOf(reminder.time) }
    val due = scheduled.filter { (_, minutes) -> minutes != null && minutes <= nowMinutes }
    if (due.isNotEmpty()) {
        return due.maxWith(compareBy({ it.second!! }, { -it.first.id })).first
    }
    val later = scheduled.filter { it.second != null }
    if (later.isNotEmpty()) {
        return later.minWith(compareBy({ it.second!! }, { it.first.id })).first
    }
    return open.first()
}

private val reminderTimeOrder =
    compareBy<Remed>({ minutesOf(it.time) ?: Int.MAX_VALUE }, { it.id })

private fun isPast(reminder: Remed, nowMinutes: Int): Boolean {
    val minutes = minutesOf(reminder.time) ?: return false
    return minutes <= nowMinutes
}

private fun isDue(reminder: Remed, now: Calendar): Boolean {
    return isPast(reminder, minutesOfDay(now))
}

private fun minutesOfDay(calendar: Calendar): Int {
    return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
}

private fun minutesOf(time: String): Int? {
    val parsed = listOf("h:mm a", "hh:mm a").firstNotNullOfOrNull { pattern ->
        runCatching { SimpleDateFormat(pattern, Locale.US).parse(time) }.getOrNull()
    } ?: return null
    val calendar = Calendar.getInstance().apply { this.time = parsed }
    return minutesOfDay(calendar)
}
