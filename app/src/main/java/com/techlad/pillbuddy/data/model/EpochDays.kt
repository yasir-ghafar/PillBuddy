package com.techlad.pillbuddy.data.model

import java.util.Calendar
import java.util.TimeZone

object EpochDays {
    private const val MILLIS_PER_DAY = 86_400_000L

    fun of(year: Int, month: Int, dayOfMonth: Int): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.clear()
        utc.set(year, month, dayOfMonth)
        return utc.timeInMillis / MILLIS_PER_DAY
    }

    fun fromMillis(millis: Long): Long {
        val local = Calendar.getInstance().apply { timeInMillis = millis }
        return of(
            local.get(Calendar.YEAR),
            local.get(Calendar.MONTH),
            local.get(Calendar.DAY_OF_MONTH),
        )
    }

    fun dayOfMonth(epochDay: Long): Int {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.timeInMillis = epochDay * MILLIS_PER_DAY
        return utc.get(Calendar.DAY_OF_MONTH)
    }

    /** Local wall-clock time on the calendar date represented by [epochDay]. */
    fun atLocalTime(epochDay: Long, hour: Int, minute: Int): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = epochDay * MILLIS_PER_DAY
        }
        return Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, utc.get(Calendar.YEAR))
            set(Calendar.MONTH, utc.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, utc.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
