package com.techlad.pillbuddy.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DoseAlarmPlanTest {
    @Test
    fun schedulesLaterTodayAtTheDoseTime() {
        val day = EpochDays.of(2026, Calendar.OCTOBER, 21)
        val now = EpochDays.atLocalTime(day, 7, 0)
        val trigger = nextDoseTrigger(reminder(day, hour = 8), now)

        assertEquals(EpochDays.atLocalTime(day, 8, 0), trigger?.triggerAtMillis)
        assertEquals(day, trigger?.epochDay)
        assertEquals(8 * 60, trigger?.scheduledMinutes)
    }

    @Test
    fun schedulesTomorrowAfterTheDoseTimeHasPassed() {
        val day = EpochDays.of(2026, Calendar.OCTOBER, 21)
        val trigger = nextDoseTrigger(reminder(day, hour = 8), EpochDays.atLocalTime(day, 8, 0))

        assertEquals(day + 1, trigger?.epochDay)
        assertEquals(EpochDays.atLocalTime(day + 1, 8, 0), trigger?.triggerAtMillis)
    }

    @Test
    fun firesNotifyMinutesBeforeTheDose() {
        val day = EpochDays.of(2026, Calendar.OCTOBER, 21)
        val trigger = nextDoseTrigger(
            reminder(day, hour = 8, notifyMinutesBefore = 15),
            EpochDays.atLocalTime(day, 7, 0),
        )

        assertEquals(EpochDays.atLocalTime(day, 8, 0) - 15 * 60_000L, trigger?.triggerAtMillis)
        assertEquals(day, trigger?.epochDay)
    }

    @Test
    fun remindsSoonWhenTheLeadTimeHasPassedButTheDoseHasNot() {
        val day = EpochDays.of(2026, Calendar.OCTOBER, 21)
        val now = EpochDays.atLocalTime(day, 17, 30)
        val doseAt = EpochDays.atLocalTime(day, 18, 0)
        val trigger = nextDoseTrigger(
            reminder(day, hour = 18, notifyMinutesBefore = 60),
            now,
            fireImmediatelyIfLeadPassed = true,
        )

        assertEquals(day, trigger?.epochDay)
        assertEquals(now + DOSE_ALARM_GRACE_MS, trigger?.triggerAtMillis)
        assertTrue((trigger?.triggerAtMillis ?: Long.MAX_VALUE) < doseAt)
    }

    @Test
    fun rescheduleWaitsForTheDoseWhenTheLeadTimeAlreadyPassed() {
        val day = EpochDays.of(2026, Calendar.OCTOBER, 21)
        val trigger = nextDoseTrigger(
            reminder(day, hour = 18, notifyMinutesBefore = 60),
            EpochDays.atLocalTime(day, 17, 30),
        )

        assertEquals(EpochDays.atLocalTime(day, 18, 0), trigger?.triggerAtMillis)
    }

    @Test
    fun weeklyReminderSkipsDaysItDoesNotOccur() {
        val start = EpochDays.of(2026, Calendar.OCTOBER, 1)
        val trigger = nextDoseTrigger(
            reminder(start, hour = 8, frequency = Frequency.WEEK),
            EpochDays.atLocalTime(start + 1, 7, 0),
        )

        assertEquals(start + 7, trigger?.epochDay)
        assertEquals(EpochDays.atLocalTime(start + 7, 8, 0), trigger?.triggerAtMillis)
    }

    @Test
    fun waitsUntilTheStartDate() {
        val today = EpochDays.of(2026, Calendar.OCTOBER, 21)
        val start = today + 2
        val trigger = nextDoseTrigger(reminder(start, hour = 8), EpochDays.atLocalTime(today, 10, 0))

        assertEquals(start, trigger?.epochDay)
        assertEquals(EpochDays.atLocalTime(start, 8, 0), trigger?.triggerAtMillis)
    }

    @Test
    fun returnsNullAfterTheEndDate() {
        val start = EpochDays.of(2026, Calendar.OCTOBER, 1)
        val reminder = reminder(start, hour = 8).copy(endEpochDay = start)

        assertNull(nextDoseTrigger(reminder, EpochDays.atLocalTime(start + 1, 7, 0)))
    }

    @Test
    fun snoozeAddsTenMinutes() {
        assertEquals(1_700_000_000_000L + 10 * 60_000L, snoozeUntil(1_700_000_000_000L))
    }

    private fun reminder(
        startEpochDay: Long,
        hour: Int,
        minute: Int = 0,
        frequency: Frequency = Frequency.DAY,
        notifyMinutesBefore: Int = 0,
    ) = Reminder(
        id = 1,
        name = "Nexium",
        instructions = "One capsule",
        hour = hour,
        minute = minute,
        frequency = frequency,
        startEpochDay = startEpochDay,
        endEpochDay = null,
        notifyMinutesBefore = notifyMinutesBefore,
    )
}
