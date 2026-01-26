package com.smarteyex.core

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceEngine(private val context: Context) {

    private lateinit var tts: TextToSpeech

    fun init() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("id", "ID")
            }
        }
    }

    fun speak(text: String) {
        if (::tts.isInitialized) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "VOICE_ENGINE")
    }

    fun shutdown() {
        if (::tts.isInitialized) tts.shutdown()
    }

    // ganti toggleListening/startListening sesuai MainActivity
    fun startListening() {
        speak("Bung Smart siap mendengar")
    }
}