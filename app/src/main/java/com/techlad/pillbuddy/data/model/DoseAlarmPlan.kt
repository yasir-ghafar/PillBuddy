package com.techlad.pillbuddy.data.model

/** How long a notification snooze waits before the dose is shown again. */
const val SNOOZE_MINUTES = 10

/** Short delay used when the early-warning time has already passed but the dose has not. */
internal const val DOSE_ALARM_GRACE_MS = 5_000L

data class DoseTrigger(
    val triggerAtMillis: Long,
    val epochDay: Long,
    val scheduledMinutes: Int,
)

fun snoozeUntil(fromMillis: Long): Long = fromMillis + SNOOZE_MINUTES * 60_000L

/**
 * Next moment a reminder should alert.
 *
 * The alert is [Reminder.notifyMinutesBefore] ahead of the dose. If that lead time is already
 * past and the dose itself is still ahead, a newly saved reminder alerts a few seconds from
 * [afterMillis]. A reschedule instead waits for the dose time, so opening the app does not
 * repeat an alert that already fired. Days after [Reminder.endEpochDay] are not scheduled.
 */
fun nextDoseTrigger(
    reminder: Reminder,
    afterMillis: Long,
    fireImmediatelyIfLeadPassed: Boolean = false,
): DoseTrigger? {
    var day = maxOf(EpochDays.fromMillis(afterMillis), reminder.startEpochDay)
    val horizon = day + 366
    val end = reminder.endEpochDay
    while (day <= horizon && (end == null || day <= end)) {
        if (reminder.occursOn(day)) {
            val doseAt = EpochDays.atLocalTime(day, reminder.hour, reminder.minute)
            val notifyAt = doseAt - reminder.notifyMinutesBefore * 60_000L
            val triggerAt = when {
                notifyAt > afterMillis -> notifyAt
                doseAt > afterMillis && fireImmediatelyIfLeadPassed -> afterMillis + DOSE_ALARM_GRACE_MS
                doseAt > afterMillis -> doseAt
                else -> null
            }
            if (triggerAt != null) {
                return DoseTrigger(
                    triggerAtMillis = triggerAt,
                    epochDay = day,
                    scheduledMinutes = reminder.hour * 60 + reminder.minute,
                )
            }
        }
        day += 1
    }
    return null
}
