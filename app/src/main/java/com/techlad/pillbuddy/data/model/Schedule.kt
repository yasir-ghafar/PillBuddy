package com.techlad.pillbuddy.data.model

/**
 * Recurrence and calendar math used by reminder storage.
 * Uses `java.util.Calendar` today. Swap this file for kotlinx-datetime when the data layer moves to KMM.
 */
enum class Frequency {
    DAY,
    WEEK,
    MONTH,
}

enum class DoseStatus {
    TAKEN,
    SNOOZED,
    MISSED,
}

enum class TextScale(val multiplier: Float) {
    DEFAULT(1f),
    LARGE(1.15f),
    EXTRA_LARGE(1.3f),
}

data class Reminder(
    val id: Long,
    val name: String,
    val instructions: String,
    val hour: Int,
    val minute: Int,
    val frequency: Frequency,
    val startEpochDay: Long,
    val endEpochDay: Long?,
    val notifyMinutesBefore: Int,
)

data class NewReminder(
    val name: String,
    val instructions: String,
    val hour: Int,
    val minute: Int,
    val frequency: Frequency,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val notifyMinutesBefore: Int,
)

data class DoseEvent(
    val id: Long,
    val reminderId: Long,
    val epochDay: Long,
    val status: DoseStatus,
    val scheduledMinutes: Int,
    val actedAtMillis: Long,
    val snoozeUntilMillis: Long?,
)

data class DoseHistoryItem(
    val id: Long,
    val reminderName: String,
    val status: DoseStatus,
    val actedAtMillis: Long,
)

data class AppSettings(
    val textScale: TextScale = TextScale.DEFAULT,
    val highContrast: Boolean = false,
    val haptics: Boolean = true,
    val spokenConfirmation: Boolean = false,
)

fun Reminder.occursOn(epochDay: Long): Boolean {
    if (epochDay < startEpochDay) return false
    val end = endEpochDay
    if (end != null && epochDay > end) return false
    return when (frequency) {
        Frequency.DAY -> true
        Frequency.WEEK -> (epochDay - startEpochDay) % 7L == 0L
        Frequency.MONTH -> EpochDays.dayOfMonth(epochDay) == EpochDays.dayOfMonth(startEpochDay)
    }
}
