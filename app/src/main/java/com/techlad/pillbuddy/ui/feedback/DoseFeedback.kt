package com.techlad.pillbuddy.ui.feedback

import android.os.Build
import android.speech.tts.TextToSpeech
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberDoseSpeaker(): (String) -> Unit {
    val context = LocalContext.current
    val speaker = remember {
        TextToSpeech(context.applicationContext) { }
    }
    DisposableEffect(speaker) {
        onDispose { speaker.shutdown() }
    }
    return { message ->
        val params: android.os.Bundle? = null
        speaker.speak(message, TextToSpeech.QUEUE_FLUSH, params, "dose-confirmation")
    }
}

fun performConfirmHaptic(view: View) {
    val feedback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        HapticFeedbackConstants.CONFIRM
    } else {
        HapticFeedbackConstants.LONG_PRESS
    }
    view.performHapticFeedback(feedback)
}
