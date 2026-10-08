package com.techlad.pillbuddy.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class TodayScheduleTest {
    @Test
    fun weeklyReminderOccursEverySevenDaysFromTheStart() {
        val start = EpochDays.of(2026, Calendar.OCTOBER, 1)
        val reminder = reminder(startEpochDay = start, frequency = Frequency.WEEK)

        assertTrue(reminder.occursOn(start))
        assertFalse(reminder.occursOn(start + 1))
        assertTrue(reminder.occursOn(start + 7))
        assertFalse(reminder.occursOn(start - 1))
    }

    @Test
    fun takenDoseIsCompletedAndSnoozeMovesTheDisplayedTime() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 8, 15, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val day = EpochDays.fromMillis(now)
        val reminder = reminder(startEpochDay = day, frequency = Frequency.DAY)
        val snoozeUntil = now + 10 * 60_000L

        val taken = scheduledDoses(
            reminders = listOf(reminder),
            events = listOf(
                dose(reminderId = reminder.id, epochDay = day, status = DoseStatus.TAKEN, at = now),
            ),
            nowMillis = now,
        )
        assertTrue(taken.single().remed.completed)
        assertEquals(formatClock(9, 0), taken.single().remed.time)

        val snoozed = scheduledDoses(
            reminders = listOf(reminder),
            events = listOf(
                dose(
                    reminderId = reminder.id,
                    epochDay = day,
                    status = DoseStatus.SNOOZED,
                    at = now,
                    snoozeUntil = snoozeUntil,
                ),
            ),
            nowMillis = now,
        )
        assertFalse(snoozed.single().remed.completed)
        assertEquals(formatMillis(snoozeUntil), snoozed.single().remed.time)
    }

    private fun reminder(startEpochDay: Long, frequency: Frequency) = Reminder(
        id = 4,
        name = "Nexium",
        instructions = "One capsule",
        hour = 9,
        minute = 0,
        frequency = frequency,
        startEpochDay = startEpochDay,
        endEpochDay = null,
        notifyMinutesBefore = 0,
    )

    private fun dose(
        reminderId: Long,
        epochDay: Long,
        status: DoseStatus,
        at: Long,
        snoozeUntil: Long? = null,
    ) = DoseEvent(
        id = 1,
        reminderId = reminderId,
        epochDay = epochDay,
        status = status,
        scheduledMinutes = 9 * 60,
        actedAtMillis = at,
        snoozeUntilMillis = snoozeUntil,
    )
}
