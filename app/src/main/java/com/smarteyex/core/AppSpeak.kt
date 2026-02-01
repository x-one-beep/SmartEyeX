package com.smarteyex.core

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class AppSpeak(private val context: Context) {

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("id", "ID")  // Bahasa Indonesia, bisa disesuaikan
            }
        }
    }

    // Fungsi untuk berbicara dengan gaya Gen Z (cepat, santai)
    fun speakGenZ(text: String) {
        val genZText = "Yo, $text bro!"  // Modifikasi sederhana untuk gaya Gen Z
        tts?.speak(genZText, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        tts?.shutdown()
    }
}