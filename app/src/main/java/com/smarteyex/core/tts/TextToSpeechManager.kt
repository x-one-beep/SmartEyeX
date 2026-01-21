package com.smarteyex.core

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class TextToSpeechManager(context: Context) {

    private var tts: TextToSpeech? = null
    var isEnabled = true

    init {
        tts = TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                tts?.language = Locale("id", "ID")
            }
        }
    }

    fun speak(text: String) {
        if (!isEnabled) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SmartEyeX_TTS")
    }

    fun stop() {
        tts?.stop()
    }

    fun release() {
        tts?.shutdown()
    }
}
