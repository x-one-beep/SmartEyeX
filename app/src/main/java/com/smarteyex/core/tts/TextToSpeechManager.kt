package com.smarteyex.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TextToSpeechManager(private val ctx: Context) {

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(ctx) { status ->
            if (status == TextToSpeech.SUCCESS) {
                try {
                    tts?.language = Locale("id", "ID")
                    tts?.setSpeechRate(0.95f)
                } catch (t: Throwable) {
                    Log.w("TTS", "setLanguage failed: ${t.message}")
                }
            } else {
                Log.w("TTS", "init failed")
            }
        }
    }

    fun speak(text: String) {
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SmartEyeXUtterance")
        } catch (t: Throwable) {
            Log.w("TTS", "speak failed: ${t.message}")
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
    }
}
