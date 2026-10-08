package com.techlad.pillbuddy

import android.app.Application
import com.techlad.pillbuddy.data.AppContainer

class PillBuddyApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
