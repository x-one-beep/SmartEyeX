package com.smarteyex.core

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class VoiceEngine(context: Context) {

    private val tts = TextToSpeech(context) {
        tts.language = Locale("id","ID")
    }

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "VOICE_ENGINE")
    }
}