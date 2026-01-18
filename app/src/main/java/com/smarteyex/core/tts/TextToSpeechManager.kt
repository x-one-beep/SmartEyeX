package com.smarteyex.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class TextToSpeechManager(context: Context) {

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("id", "ID")
            }
        }
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
