package com.smarteyex.core

import android.content.Context

class VoiceEngine(private val context: Context) {

    private val appSpeak = AppSpeak(context)

    // Fungsi untuk speak response AI atau notifikasi
    fun speak(text: String) {
        appSpeak.speakGenZ(text)
    }

    // Fungsi untuk speak notifikasi WA di background
    fun speakNotification(notification: String) {
        speak("New WA notification: $notification")
    }

    // Shutdown untuk cleanup
    fun shutdown() {
        appSpeak.shutdown()
    }
}