package com.techlad.pillbuddy.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ScheduledDose(
    val remed: Remed,
    val scheduledMinutes: Int,
    val epochDay: Long,
)

fun scheduledDoses(
    reminders: List<Reminder>,
    events: List<DoseEvent>,
    nowMillis: Long,
): List<ScheduledDose> {
    val epochDay = EpochDays.fromMillis(nowMillis)
    return reminders.mapNotNull { reminder ->
        if (!reminder.occursOn(epochDay)) return@mapNotNull null
        val todays = events.filter { it.reminderId == reminder.id && it.epochDay == epochDay }
        val taken = todays.any { it.status == DoseStatus.TAKEN }
        val activeSnooze = todays
            .filter { event ->
                event.status == DoseStatus.SNOOZED &&
                    (event.snoozeUntilMillis ?: 0L) > nowMillis
            }
            .maxByOrNull { it.actedAtMillis }
        val timeLabel = if (activeSnooze?.snoozeUntilMillis != null) {
            formatMillis(activeSnooze.snoozeUntilMillis)
        } else {
            formatClock(reminder.hour, reminder.minute)
        }
        ScheduledDose(
            remed = Remed(
                id = reminder.id,
                name = reminder.name,
                instructions = reminder.instructions,
                time = timeLabel,
                completed = taken,
            ),
            scheduledMinutes = reminder.hour * 60 + reminder.minute,
            epochDay = epochDay,
        )
    }
}

fun formatClock(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return SimpleDateFormat("h:mm a", Locale.US).format(calendar.time)
}

fun formatMillis(millis: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = millis }
    return formatClock(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
}
