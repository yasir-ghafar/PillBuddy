package com.techlad.pillbuddy.notification

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.techlad.pillbuddy.R

@Composable
fun DoseReminderPermissions(enabled: Boolean) {
    val context = LocalContext.current
    var asked by rememberSaveable { mutableStateOf(false) }
    var showExactAlarm by rememberSaveable { mutableStateOf(false) }
    var showNotificationsOff by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            showExactAlarm = !context.canUseExactAlarms()
        } else {
            showNotificationsOff = true
        }
    }

    LaunchedEffect(enabled) {
        if (!enabled || asked) return@LaunchedEffect
        asked = true
        if (needsNotificationPermission(context)) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            showExactAlarm = !context.canUseExactAlarms()
        }
    }

    if (showExactAlarm) {
        AlertDialog(
            onDismissRequest = { showExactAlarm = false },
            title = { Text(stringResource(R.string.exact_alarm_title)) },
            text = { Text(stringResource(R.string.exact_alarm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExactAlarm = false
                        context.openExactAlarmSettings()
                    },
                ) {
                    Text(stringResource(R.string.exact_alarm_allow))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExactAlarm = false }) {
                    Text(stringResource(R.string.exact_alarm_not_now))
                }
            },
        )
    }

    if (showNotificationsOff) {
        AlertDialog(
            onDismissRequest = { showNotificationsOff = false },
            title = { Text(stringResource(R.string.notifications_off_title)) },
            text = { Text(stringResource(R.string.notifications_off_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNotificationsOff = false
                        context.openNotificationSettings()
                    },
                ) {
                    Text(stringResource(R.string.notifications_off_open))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationsOff = false }) {
                    Text(stringResource(R.string.exact_alarm_not_now))
                }
            },
        )
    }
}

private fun needsNotificationPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) != PackageManager.PERMISSION_GRANTED
}

private fun Context.openExactAlarmSettings() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val specific = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.parse("package:$packageName")
    }
    val generic = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
    runCatching { startActivity(specific) }.onFailure { startActivity(generic) }
}

private fun Context.openNotificationSettings() {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
    }
    startActivity(intent)
}
