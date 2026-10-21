package com.techlad.pillbuddy.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.techlad.pillbuddy.data.model.DoseTrigger
import com.techlad.pillbuddy.data.model.EpochDays
import com.techlad.pillbuddy.data.model.Reminder
import com.techlad.pillbuddy.data.model.nextDoseTrigger
import com.techlad.pillbuddy.data.repository.DoseHistoryRepository
import com.techlad.pillbuddy.data.repository.ReminderRepository

/**
 * Exact alarms for dose reminders and snoozes.
 * Each reminder keeps one upcoming dose alarm. A snooze is a separate one-shot alarm.
 */
class DoseAlarmScheduler(
    private val context: Context,
    private val reminders: ReminderRepository,
    private val doseHistory: DoseHistoryRepository,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val alertedDays = context.getSharedPreferences(ALERTED_PREFS, Context.MODE_PRIVATE)

    suspend fun scheduleReminder(
        reminder: Reminder,
        afterMillis: Long = System.currentTimeMillis(),
        fireImmediatelyIfLeadPassed: Boolean = false,
    ) {
        var after = afterMillis
        var alertNow = fireImmediatelyIfLeadPassed
        val now = System.currentTimeMillis()
        repeat(SCHEDULE_ATTEMPTS) {
            val trigger = nextDoseTrigger(reminder, after, alertNow) ?: run {
                cancelDose(reminder.id)
                return
            }
            alertNow = false
            val taken = doseHistory.isTaken(reminder.id, trigger.epochDay)
            val snoozed = doseHistory.hasPendingSnooze(reminder.id, trigger.epochDay, now)
            if (!taken && !snoozed && !wasAlerted(reminder.id, trigger.epochDay)) {
                arm(reminder.id, trigger, KIND_DOSE, SLOT_DOSE)
                return
            }
            after = EpochDays.atLocalTime(trigger.epochDay, reminder.hour, reminder.minute)
        }
        cancelDose(reminder.id)
    }

    /** Schedules the occurrence after [epochDay], leaving that day's dose alone. */
    suspend fun scheduleFollowing(reminder: Reminder, epochDay: Long) {
        val afterDose = EpochDays.atLocalTime(epochDay, reminder.hour, reminder.minute)
        scheduleReminder(reminder, afterDose)
    }

    suspend fun onTaken(reminderId: Long) {
        cancelSnooze(reminderId)
        DoseNotifications.cancel(context, reminderId)
        reminders.getById(reminderId)?.let { scheduleReminder(it) }
    }

    suspend fun onSnoozed(
        reminderId: Long,
        epochDay: Long,
        scheduledMinutes: Int,
        snoozeUntilMillis: Long,
    ) {
        DoseNotifications.cancel(context, reminderId)
        arm(
            reminderId = reminderId,
            trigger = DoseTrigger(
                triggerAtMillis = snoozeUntilMillis,
                epochDay = epochDay,
                scheduledMinutes = scheduledMinutes,
            ),
            kind = KIND_SNOOZE,
            slot = SLOT_SNOOZE,
        )
        reminders.getById(reminderId)?.let { scheduleReminder(it) }
    }

    suspend fun rescheduleAll(nowMillis: Long = System.currentTimeMillis()) {
        reminders.listAll().forEach { scheduleReminder(it, nowMillis) }
        doseHistory.activeSnoozes(nowMillis)
            .groupBy { it.reminderId }
            .values
            .mapNotNull { events -> events.maxByOrNull { it.actedAtMillis } }
            .forEach { event ->
                val until = event.snoozeUntilMillis ?: return@forEach
                if (doseHistory.isTaken(event.reminderId, event.epochDay)) return@forEach
                arm(
                    reminderId = event.reminderId,
                    trigger = DoseTrigger(until, event.epochDay, event.scheduledMinutes),
                    kind = KIND_SNOOZE,
                    slot = SLOT_SNOOZE,
                )
            }
    }

    fun markAlerted(reminderId: Long, epochDay: Long) {
        alertedDays.edit().putLong(reminderId.toString(), epochDay).apply()
    }

    fun cancel(reminderId: Long) {
        cancelDose(reminderId)
        cancelSnooze(reminderId)
        DoseNotifications.cancel(context, reminderId)
    }

    private fun cancelDose(reminderId: Long) {
        cancelAlarm(reminderId, SLOT_DOSE)
    }

    private fun cancelSnooze(reminderId: Long) {
        cancelAlarm(reminderId, SLOT_SNOOZE)
    }

    private fun arm(reminderId: Long, trigger: DoseTrigger, kind: String, slot: Int) {
        val pending = pendingBroadcast(
            requestCode = requestCode(reminderId, slot),
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        ) {
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_EPOCH_DAY, trigger.epochDay)
            putExtra(EXTRA_SCHEDULED_MINUTES, trigger.scheduledMinutes)
            putExtra(EXTRA_KIND, kind)
        } ?: return
        scheduleAt(trigger.triggerAtMillis, pending)
    }

    private fun cancelAlarm(reminderId: Long, slot: Int) {
        val pending = pendingBroadcast(
            requestCode = requestCode(reminderId, slot),
            flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(pending)
        pending.cancel()
    }

    private fun pendingBroadcast(
        requestCode: Int,
        flags: Int,
        fill: Intent.() -> Unit = {},
    ): PendingIntent? {
        val intent = Intent(context, DoseAlarmReceiver::class.java).apply {
            action = ACTION_SHOW
            fill()
        }
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    private fun wasAlerted(reminderId: Long, epochDay: Long): Boolean {
        return alertedDays.getLong(reminderId.toString(), Long.MIN_VALUE) == epochDay
    }

    private fun scheduleAt(triggerAtMillis: Long, pending: PendingIntent) {
        try {
            if (context.canUseExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        }
    }

    private companion object {
        const val SCHEDULE_ATTEMPTS = 24
        const val ALERTED_PREFS = "dose_alert_days"
    }
}

fun Context.canUseExactAlarms(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val alarmManager = getSystemService(AlarmManager::class.java) ?: return false
    return alarmManager.canScheduleExactAlarms()
}
