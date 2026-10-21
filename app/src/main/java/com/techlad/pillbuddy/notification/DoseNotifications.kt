package com.techlad.pillbuddy.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.techlad.pillbuddy.MainActivity
import com.techlad.pillbuddy.R
import com.techlad.pillbuddy.data.model.Reminder
import com.techlad.pillbuddy.data.model.formatClock

internal const val ACTION_SHOW = "com.techlad.pillbuddy.action.SHOW_DOSE"
internal const val ACTION_DONE = "com.techlad.pillbuddy.action.DOSE_DONE"
internal const val ACTION_SNOOZE = "com.techlad.pillbuddy.action.DOSE_SNOOZE"

internal const val EXTRA_REMINDER_ID = "reminder_id"
internal const val EXTRA_EPOCH_DAY = "epoch_day"
internal const val EXTRA_SCHEDULED_MINUTES = "scheduled_minutes"
internal const val EXTRA_KIND = "kind"

internal const val KIND_DOSE = "dose"
internal const val KIND_SNOOZE = "snooze"

private const val CHANNEL_ID = "dose_reminders"
private const val SLOTS = 4
internal const val SLOT_DOSE = 0
internal const val SLOT_SNOOZE = 1
private const val SLOT_DONE = 2
private const val SLOT_SNOOZE_ACTION = 3

internal fun requestCode(reminderId: Long, slot: Int): Int = (reminderId * SLOTS + slot).toInt()

object DoseNotifications {
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            enableVibration(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }

    fun show(
        context: Context,
        reminder: Reminder,
        epochDay: Long,
        scheduledMinutes: Int,
        kind: String,
    ): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        ensureChannel(context)
        val timeLine = if (kind == KIND_SNOOZE) {
            context.getString(R.string.notification_due_now)
        } else {
            context.getString(R.string.notification_due_at, formatClock(reminder.hour, reminder.minute))
        }
        val collapsed = RemoteViews(context.packageName, R.layout.notification_dose).apply {
            setTextViewText(R.id.notification_title, reminder.name)
            setTextViewText(R.id.notification_time, timeLine)
        }
        val expanded = RemoteViews(context.packageName, R.layout.notification_dose_expanded).apply {
            setTextViewText(R.id.notification_title, reminder.name)
            setTextViewText(R.id.notification_time, timeLine)
            if (reminder.instructions.isBlank()) {
                setViewVisibility(R.id.notification_body, View.GONE)
            } else {
                setViewVisibility(R.id.notification_body, View.VISIBLE)
                setTextViewText(R.id.notification_body, reminder.instructions)
            }
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_dose)
            .setColor(context.getColor(R.color.pill_buddy_dark))
            .setContentTitle(reminder.name)
            .setContentText(timeLine)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsed)
            .setCustomBigContentView(expanded)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOnlyAlertOnce(false)
            .setContentIntent(openAppIntent(context, reminder.id))
            .addAction(doneAction(context, reminder.id, epochDay, scheduledMinutes))
            .addAction(snoozeAction(context, reminder.id, epochDay, scheduledMinutes))
            .build()
        return try {
            NotificationManagerCompat.from(context).notify(reminder.id.toInt(), notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun cancel(context: Context, reminderId: Long) {
        NotificationManagerCompat.from(context).cancel(reminderId.toInt())
    }

    private fun doneAction(
        context: Context,
        reminderId: Long,
        epochDay: Long,
        scheduledMinutes: Int,
    ): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(
            R.drawable.ic_notification_done,
            context.getString(R.string.done),
            actionIntent(context, ACTION_DONE, reminderId, epochDay, scheduledMinutes, SLOT_DONE),
        ).build()
    }

    private fun snoozeAction(
        context: Context,
        reminderId: Long,
        epochDay: Long,
        scheduledMinutes: Int,
    ): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(
            R.drawable.ic_notification_snooze,
            context.getString(R.string.snooze),
            actionIntent(context, ACTION_SNOOZE, reminderId, epochDay, scheduledMinutes, SLOT_SNOOZE_ACTION),
        ).build()
    }

    private fun actionIntent(
        context: Context,
        action: String,
        reminderId: Long,
        epochDay: Long,
        scheduledMinutes: Int,
        slot: Int,
    ): PendingIntent {
        val intent = Intent(context, DoseAlarmReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_EPOCH_DAY, epochDay)
            putExtra(EXTRA_SCHEDULED_MINUTES, scheduledMinutes)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode(reminderId, slot),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun openAppIntent(context: Context, reminderId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
