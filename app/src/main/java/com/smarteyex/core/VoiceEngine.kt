package com.smarteyex.core

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceEngine(private val context: Context) {

    private var isReady = false

fun init() {
    tts = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("id", "ID")
            isReady = true
        }
    }
}

fun speak(text: String) {
    if (::tts.isInitialized && isReady) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "VOICE_ENGINE")
    }
}