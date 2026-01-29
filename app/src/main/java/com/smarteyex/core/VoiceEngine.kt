package com.smarteyex.core

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceEngine(context: Context) : TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var isReady: Boolean = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("id", "ID")
            isReady = true
        }
    }

    fun speak(text: String) {
        if (!isReady) return

        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "SMART_EYE_X"
        )
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}