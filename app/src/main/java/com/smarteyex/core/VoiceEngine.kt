package com.smarteyex.core

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class VoiceEngine(context: Context) {

    private var isReady = false
    private val tts = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("id", "ID")
            isReady = true
        }
    }

    fun speak(text: String) {
        if (isReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SMART_EYE_X")
        }
    }

    fun shutdown() {
        tts.shutdown()
    }
}