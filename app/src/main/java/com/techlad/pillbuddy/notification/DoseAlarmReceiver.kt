package com.techlad.pillbuddy.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.techlad.pillbuddy.PillBuddyApplication
import com.techlad.pillbuddy.data.model.DoseStatus
import com.techlad.pillbuddy.data.model.snoozeUntil
import kotlinx.coroutines.launch

class DoseAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val pending = goAsync()
        val app = context.applicationContext as PillBuddyApplication
        app.applicationScope.launch {
            try {
                handle(context.applicationContext, intent, app)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handle(context: Context, intent: Intent, app: PillBuddyApplication) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        val epochDay = intent.getLongExtra(EXTRA_EPOCH_DAY, -1L)
        val scheduledMinutes = intent.getIntExtra(EXTRA_SCHEDULED_MINUTES, 0)
        if (reminderId < 0L || epochDay < 0L) return
        val container = app.container
        when (intent.action) {
            ACTION_DONE -> {
                container.doseHistory.record(
                    reminderId = reminderId,
                    epochDay = epochDay,
                    status = DoseStatus.TAKEN,
                    scheduledMinutes = scheduledMinutes,
                    actedAtMillis = System.currentTimeMillis(),
                    snoozeUntilMillis = null,
                )
                container.doseAlarms.onTaken(reminderId)
            }

            ACTION_SNOOZE -> {
                val actedAt = System.currentTimeMillis()
                val until = snoozeUntil(actedAt)
                container.doseHistory.record(
                    reminderId = reminderId,
                    epochDay = epochDay,
                    status = DoseStatus.SNOOZED,
                    scheduledMinutes = scheduledMinutes,
                    actedAtMillis = actedAt,
                    snoozeUntilMillis = until,
                )
                container.doseAlarms.onSnoozed(
                    reminderId = reminderId,
                    epochDay = epochDay,
                    scheduledMinutes = scheduledMinutes,
                    snoozeUntilMillis = until,
                )
            }

            ACTION_SHOW -> {
                val reminder = container.reminders.getById(reminderId)
                if (reminder == null) {
                    container.doseAlarms.cancel(reminderId)
                    return
                }
                val kind = intent.getStringExtra(EXTRA_KIND) ?: KIND_DOSE
                val taken = container.doseHistory.isTaken(reminderId, epochDay)
                val coveredBySnooze = kind == KIND_DOSE &&
                    container.doseHistory.hasPendingSnooze(reminderId, epochDay, System.currentTimeMillis())
                if (!taken && !coveredBySnooze) {
                    val posted = DoseNotifications.show(
                        context = context,
                        reminder = reminder,
                        epochDay = epochDay,
                        scheduledMinutes = scheduledMinutes,
                        kind = kind,
                    )
                    if (posted) container.doseAlarms.markAlerted(reminderId, epochDay)
                } else {
                    DoseNotifications.cancel(context, reminderId)
                }
                container.doseAlarms.scheduleFollowing(reminder, epochDay)
            }
        }
    }
}
