package com.techlad.pillbuddy

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.techlad.pillbuddy.data.model.AppSettings
import com.techlad.pillbuddy.ui.navigation.PillBuddyApp
import com.techlad.pillbuddy.ui.theme.PillBuddyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        val container = (application as PillBuddyApplication).container
        setContent {
            val settings by container.preferences.settings.collectAsState(initial = AppSettings())
            PillBuddyTheme(
                textScale = settings.textScale,
                highContrast = settings.highContrast,
            ) {
                PillBuddyApp(container = container, settings = settings)
            }
        }
    }
}
