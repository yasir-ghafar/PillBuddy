package com.techlad.pillbuddy.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.techlad.pillbuddy.PillBuddyApplication
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pending = goAsync()
        val app = context.applicationContext as PillBuddyApplication
        app.applicationScope.launch {
            try {
                app.container.doseAlarms.rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }
}
