package com.smarteyex.core.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class VoiceEngine(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var ready = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("id", "ID")
            tts?.setSpeechRate(1.0f)
            tts?.setPitch(1.05f)
            ready = true
        }
    }

    fun speak(text: String) {
        if (!ready || text.isBlank()) return
        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            System.currentTimeMillis().toString()
        )
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
