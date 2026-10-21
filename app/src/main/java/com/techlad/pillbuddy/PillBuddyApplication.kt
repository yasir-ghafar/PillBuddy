package com.techlad.pillbuddy

import android.app.Application
import com.techlad.pillbuddy.data.AppContainer
import com.techlad.pillbuddy.notification.DoseNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class PillBuddyApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        DoseNotifications.ensureChannel(this)
    }
}
