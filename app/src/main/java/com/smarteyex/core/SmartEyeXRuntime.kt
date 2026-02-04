package com.smarteyex.core

import kotlinx.coroutines.*
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import android.os.SystemClock

/* =========================================================
SmartEyeX Runtime Extensions
TIDAK MENGUBAH SmartEyeXServiceFinal
HANYA MENAMBAH BEHAVIOR
========================================================= */

/* --------------------------------------------
Global Runtime State Tambahan (AMAN)
-------------------------------------------- */
object SmartEyeXRuntime {

    var lastVoiceDetectedAt: Long = 0L
    var lastMotionDetectedAt: Long = 0L
    var lastNotificationAt: Long = 0L
    var lastUserInteractionAt: Long = 0L

    var isEnvironmentSilent: Boolean = true
    var isDeviceIdle: Boolean = true
    var trustLevel: Int = 1   // makin lama dipakai makin naik
}

/* --------------------------------------------
User Busy Intelligent Resolver
-------------------------------------------- */
fun AppState.resolveUserBusySmartly() {
    val now = SystemClock.elapsedRealtime()

    val recentlyVoice =
        now - SmartEyeXRuntime.lastVoiceDetectedAt < 2500L

    val recentlyNotif =
        now - SmartEyeXRuntime.lastNotificationAt < 2000L

    val recentlyMotion =
        now - SmartEyeXRuntime.lastMotionDetectedAt < 2000L

    // RULE UTAMA YANG LO MINTA:
    // - HP diem + ada suara = NOT busy
    // - Sepi + lama idle = busy
    userBusy = when {
        recentlyVoice -> false
        recentlyMotion -> false
        recentlyNotif -> false
        SmartEyeXRuntime.isEnvironmentSilent && SmartEyeXRuntime.isDeviceIdle -> true
        else -> false
    }
}

/* --------------------------------------------
Natural AI Timing Engine
-------------------------------------------- */
suspend fun naturalAIDelay(emotion: Emotion) {
    val delayTime = when (emotion) {
        Emotion.HAPPY -> (300L..600L).random()
        Emotion.SAD -> (800L..1200L).random()
        Emotion.ANGRY -> (500L..700L).random()
        Emotion.OVERWHELMED -> (1200L..1600L).random()
        else -> (400L..800L).random()
    }
    delay(delayTime)
}

/* --------------------------------------------
Emotion Drift Engine (AI makin "manusia")
-------------------------------------------- */
fun AppState.updateEmotionFromInteraction(text: String) {
    when {
        text.contains("makas", true) -> currentEmotion = Emotion.HAPPY
        text.contains("capek", true) -> currentEmotion = Emotion.SAD
        text.contains("anjing", true) -> currentEmotion = Emotion.ANGRY
        text.length > 120 -> currentEmotion = Emotion.OVERWHELMED
        else -> currentEmotion = Emotion.NEUTRAL
    }
}

/* --------------------------------------------
Voice Interaction Hook (AMAN)
-------------------------------------------- */
fun VoiceEngine.attachSmartEyeXHooks() {
    SmartEyeXRuntime.lastVoiceDetectedAt = SystemClock.elapsedRealtime()
    SmartEyeXRuntime.isEnvironmentSilent = false
}

/* --------------------------------------------
Notification Hook (AMAN)
-------------------------------------------- */
fun NotificationListener.markNotificationArrived() {
    SmartEyeXRuntime.lastNotificationAt = SystemClock.elapsedRealtime()
}

/* --------------------------------------------
Motion Hook (AMAN)
-------------------------------------------- */
fun MotionAnalyzer.markMotionDetected() {
    SmartEyeXRuntime.lastMotionDetectedAt = SystemClock.elapsedRealtime()
    SmartEyeXRuntime.isDeviceIdle = false
}

/* --------------------------------------------
Background Safety Watchdog
-------------------------------------------- */
fun LifecycleService.startSmartEyeXWatchdog(appState: AppState) {
    lifecycleScope.launch {
        while (true) {
            appState.resolveUserBusySmartly()

            // AI nggak boleh overreact
            if (appState.isConversationCrowded) {
                appState.currentEmotion = Emotion.OVERWHELMED
            }

            delay(1000)
        }
    }
}

/* --------------------------------------------
Trust Growth System (AI makin dekat)
-------------------------------------------- */
fun increaseTrust() {
    if (SmartEyeXRuntime.trustLevel < 10) {
        SmartEyeXRuntime.trustLevel++
    }
}

/* --------------------------------------------
Context Evolution
-------------------------------------------- */
fun AppState.evolveContext() {
    currentContext = when (currentEmotion) {
        Emotion.HAPPY -> "friendly"
        Emotion.SAD -> "supportive"
        Emotion.ANGRY -> "calm_down"
        Emotion.OVERWHELMED -> "silent_guard"
        else -> "default"
    }
}

/* --------------------------------------------
Failsafe Anti-Spam AI
-------------------------------------------- */
fun AppState.canAISpeakNow(): Boolean {
    if (isConversationCrowded) return false
    if (userBusy) return false
    if (currentEmotion == Emotion.OVERWHELMED) return false
    return true
}