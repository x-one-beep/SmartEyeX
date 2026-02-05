package com.smarteyex.core.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

object SpeechOutput {

    private lateinit var tts: TextToSpeech

    fun init(ctx: Context) {
        tts = TextToSpeech(ctx) {
            tts.language = Locale("id", "ID")
            tts.setSpeechRate(0.9f)
        }
    }

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "smarteyex_voice")
    }
}