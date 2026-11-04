package com.techlad.pillbuddy.ui.screens.new_reminder

import com.techlad.pillbuddy.data.model.EpochDays
import com.techlad.pillbuddy.data.model.Frequency
import com.techlad.pillbuddy.data.model.NewReminder
import com.techlad.pillbuddy.data.model.formatClock
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

internal enum class ScheduleEvery { Day, Week, Month }

internal val notificationLeadMinutes = listOf(0, 5, 15, 30, 60)

internal fun ScheduleEvery.toFrequency(): Frequency = when (this) {
    ScheduleEvery.Day -> Frequency.DAY
    ScheduleEvery.Week -> Frequency.WEEK
    ScheduleEvery.Month -> Frequency.MONTH
}

internal fun Frequency.toScheduleEvery(): ScheduleEvery = when (this) {
    Frequency.DAY -> ScheduleEvery.Day
    Frequency.WEEK -> ScheduleEvery.Week
    Frequency.MONTH -> ScheduleEvery.Month
}

internal fun newReminderDraft(
    name: String,
    instructions: String,
    every: ScheduleEvery,
    startDate: FormDate,
    endDate: FormDate,
    hour: Int,
    minute: Int,
    notifyMinutesBefore: Int,
): NewReminder? {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return null
    return NewReminder(
        name = trimmed,
        instructions = instructions.trim(),
        hour = hour,
        minute = minute,
        frequency = every.toFrequency(),
        startEpochDay = startDate.toEpochDay(),
        endEpochDay = endDate.toEpochDay(),
        notifyMinutesBefore = notifyMinutesBefore,
    )
}

/** Returns the picked start date and an end date that is not before it. */
internal fun applyStartDate(endDate: FormDate, picked: FormDate): Pair<FormDate, FormDate> {
    val end = if (endDate.toUtcMillis() < picked.toUtcMillis()) picked else endDate
    return picked to end
}

/** Returns [picked], or [startDate] when the pick would end before the reminder starts. */
internal fun applyEndDate(startDate: FormDate, picked: FormDate): FormDate {
    return if (picked.toUtcMillis() < startDate.toUtcMillis()) startDate else picked
}

internal data class FormDate(val year: Int, val month: Int, val day: Int) {
    fun toUtcMillis(): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.clear()
        utc.set(year, month, day)
        return utc.timeInMillis
    }

    fun format(): String {
        val local = Calendar.getInstance()
        local.clear()
        local.set(year, month, day)
        return SimpleDateFormat("d MMM yyyy", Locale.US).format(local.time)
    }

    fun toEpochDay(): Long = EpochDays.of(year, month, day)

    fun plusYears(years: Int): FormDate {
        val local = Calendar.getInstance()
        local.clear()
        local.set(year, month, day)
        local.add(Calendar.YEAR, years)
        return FormDate(
            local.get(Calendar.YEAR),
            local.get(Calendar.MONTH),
            local.get(Calendar.DAY_OF_MONTH),
        )
    }

    companion object {
        fun today(): FormDate {
            val local = Calendar.getInstance()
            return FormDate(
                local.get(Calendar.YEAR),
                local.get(Calendar.MONTH),
                local.get(Calendar.DAY_OF_MONTH),
            )
        }

        fun fromUtcMillis(millis: Long): FormDate {
            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utc.timeInMillis = millis
            return FormDate(
                utc.get(Calendar.YEAR),
                utc.get(Calendar.MONTH),
                utc.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}

internal data class RepeatRow(val label: String, val time: String)

internal fun upcomingRepeats(
    every: ScheduleEvery,
    hour: Int,
    minute: Int,
    hourCycle: Int,
    todayLabel: String,
    tomorrowLabel: String,
): List<RepeatRow> {
    val timeText = formatClock(hour, minute, hourCycle)
    val dayFormat = SimpleDateFormat("EEE, MMM d", Locale.US)
    val start = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val tomorrow = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
    return List(3) { index ->
        val occurrence = (start.clone() as Calendar).apply {
            when (every) {
                ScheduleEvery.Day -> add(Calendar.DAY_OF_YEAR, index)
                ScheduleEvery.Week -> add(Calendar.WEEK_OF_YEAR, index)
                ScheduleEvery.Month -> add(Calendar.MONTH, index)
            }
        }
        val label = when {
            isSameDay(occurrence, start) -> todayLabel
            isSameDay(occurrence, tomorrow) -> tomorrowLabel
            else -> dayFormat.format(occurrence.time)
        }
        RepeatRow(label, timeText)
    }
}

private fun isSameDay(first: Calendar, second: Calendar): Boolean {
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
}
