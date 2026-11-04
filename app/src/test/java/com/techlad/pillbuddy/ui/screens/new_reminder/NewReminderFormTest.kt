package com.techlad.pillbuddy.ui.screens.new_reminder

import com.techlad.pillbuddy.data.model.EpochDays
import com.techlad.pillbuddy.data.model.Frequency
import com.techlad.pillbuddy.data.model.formatClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewReminderFormTest {
    @Test
    fun blankNameIsRejectedAndSurroundingSpaceIsTrimmed() {
        val start = FormDate(2026, Calendar.OCTOBER, 1)
        val end = FormDate(2027, Calendar.OCTOBER, 1)

        assertNull(
            newReminderDraft(
                name = "   ",
                instructions = "Take with water",
                every = ScheduleEvery.Day,
                startDate = start,
                endDate = end,
                hour = 8,
                minute = 30,
                notifyMinutesBefore = 15,
            )
        )

        val draft = newReminderDraft(
            name = "  Vitamin D  ",
            instructions = "  with breakfast  ",
            every = ScheduleEvery.Week,
            startDate = start,
            endDate = end,
            hour = 8,
            minute = 30,
            notifyMinutesBefore = 15,
        )

        assertEquals("Vitamin D", draft?.name)
        assertEquals("with breakfast", draft?.instructions)
        assertEquals(Frequency.WEEK, draft?.frequency)
        assertEquals(8, draft?.hour)
        assertEquals(30, draft?.minute)
        assertEquals(15, draft?.notifyMinutesBefore)
        assertEquals(start.toEpochDay(), draft?.startEpochDay)
        assertEquals(end.toEpochDay(), draft?.endEpochDay)
    }

    @Test
    fun emptyInstructionsStayEmpty() {
        val day = FormDate(2026, Calendar.OCTOBER, 1)
        val draft = newReminderDraft(
            name = "Nexium",
            instructions = "   ",
            every = ScheduleEvery.Month,
            startDate = day,
            endDate = day,
            hour = 6,
            minute = 0,
            notifyMinutesBefore = 0,
        )

        assertEquals("", draft?.instructions)
        assertEquals(Frequency.MONTH, draft?.frequency)
    }

    @Test
    fun frequencyMapsBothWays() {
        assertEquals(Frequency.DAY, ScheduleEvery.Day.toFrequency())
        assertEquals(Frequency.WEEK, ScheduleEvery.Week.toFrequency())
        assertEquals(Frequency.MONTH, ScheduleEvery.Month.toFrequency())
        Frequency.entries.forEach { frequency ->
            assertEquals(frequency, frequency.toScheduleEvery().toFrequency())
        }
    }

    @Test
    fun startDatePullsTheEndForwardAndEndCannotPrecedeTheStart() {
        val start = FormDate(2026, Calendar.OCTOBER, 1)
        val end = FormDate(2026, Calendar.OCTOBER, 10)
        val later = FormDate(2026, Calendar.OCTOBER, 20)
        val earlier = FormDate(2026, Calendar.SEPTEMBER, 1)

        val (movedStart, movedEnd) = applyStartDate(end, later)
        assertEquals(later, movedStart)
        assertEquals(later, movedEnd)

        val (keptStart, keptEnd) = applyStartDate(end, FormDate(2026, Calendar.OCTOBER, 5))
        assertEquals(FormDate(2026, Calendar.OCTOBER, 5), keptStart)
        assertEquals(end, keptEnd)

        assertEquals(start, applyEndDate(start, earlier))
        assertEquals(later, applyEndDate(start, later))
        assertEquals(start, applyEndDate(start, start))
    }

    @Test
    fun formDateRoundTripsThroughUtcAndEpochDays() {
        val date = FormDate(2026, Calendar.MARCH, 1)

        assertEquals(date, FormDate.fromUtcMillis(date.toUtcMillis()))
        assertEquals(EpochDays.of(2026, Calendar.MARCH, 1), date.toEpochDay())
        assertEquals("1 Mar 2026", date.format())
        assertEquals(FormDate(2027, Calendar.MARCH, 1), date.plusYears(1))
        assertEquals(
            FormDate(2025, Calendar.FEBRUARY, 28),
            FormDate(2024, Calendar.FEBRUARY, 29).plusYears(1),
        )
    }

    @Test
    fun todayUsesTheLocalCalendar() {
        val today = FormDate.today()
        val calendar = Calendar.getInstance()

        assertEquals(calendar.get(Calendar.YEAR), today.year)
        assertEquals(calendar.get(Calendar.MONTH), today.month)
        assertEquals(calendar.get(Calendar.DAY_OF_MONTH), today.day)
    }

    @Test
    fun repeatsShowTheDoseTimeForTheNextThreeOccurrences() {
        val daily = upcomingRepeats(ScheduleEvery.Day, 8, 5, 24, "Today", "Tomorrow")
        assertEquals(listOf("Today", "Tomorrow", dayLabel(days = 2)), daily.map { it.label })
        assertTrue(daily.all { it.time == formatClock(8, 5, 24) })

        val weekly = upcomingRepeats(ScheduleEvery.Week, 6, 0, 12, "Today", "Tomorrow")
        assertEquals("Today", weekly[0].label)
        assertEquals(dayLabel(weeks = 1), weekly[1].label)
        assertEquals(dayLabel(weeks = 2), weekly[2].label)
        assertTrue(weekly.all { it.time == formatClock(6, 0, 12) })

        val monthly = upcomingRepeats(ScheduleEvery.Month, 18, 0, 24, "Today", "Tomorrow")
        assertEquals("Today", monthly[0].label)
        assertEquals(dayLabel(months = 1), monthly[1].label)
        assertEquals(dayLabel(months = 2), monthly[2].label)
    }

    @Test
    fun notificationChoicesAreTheLeadTimesShownOnTheForm() {
        assertEquals(listOf(0, 5, 15, 30, 60), notificationLeadMinutes)
    }

    private fun dayLabel(days: Int = 0, weeks: Int = 0, months: Int = 0): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, days)
            add(Calendar.WEEK_OF_YEAR, weeks)
            add(Calendar.MONTH, months)
        }
        return SimpleDateFormat("EEE, MMM d", Locale.US).format(calendar.time)
    }
}
