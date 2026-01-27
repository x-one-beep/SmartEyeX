package com.smarteyex.core

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceEngine(private val context: Context) {

    private lateinit var tts: TextToSpeech
    private var isReady = false

    fun init(onReady: (() -> Unit)? = null) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("id", "ID")
                isReady = true
                onReady?.invoke()
            }
        }
    }

    fun speak(text: String) {
        if (::tts.isInitialized && isReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "VOICE_ENGINE")
        }
    }

    fun shutdown() {
        if (::tts.isInitialized) tts.shutdown()
    }

    // AKSES TTS BUAT SERVICE
    fun getTts(): TextToSpeech? {
        return if (::tts.isInitialized) tts else null
    }
}