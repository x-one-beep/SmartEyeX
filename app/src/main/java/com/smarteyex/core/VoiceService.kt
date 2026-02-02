package com.smarteyex.core.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.smarteyex.core.state.AppState
import kotlinx.coroutines.*
import kotlin.random.Random

data class VoiceResponse(
    val text: String,
    val emotion: AppState.Emotion,
    val shouldSpeak: Boolean = true
)

class VoiceService(private val context: Context, private val appState: AppState) {

    private var speakJob: Job? = null

    /**
     * Speak text dengan emosi dan tempo manusia
     */
    fun speak(text: String, emotion: AppState.Emotion) {
        if (!appState.isMicAllowed()) return
        if (appState.isConversationCrowded) return

        speakJob?.cancel()
        speakJob = CoroutineScope(Dispatchers.IO).launch {
            val finalText = applyEmotionStyle(text, emotion)
            simulateSpeechOutput(finalText)
        }
    }

    /**
     * Tambahkan style / intonasi sesuai emosi
     */
    private fun applyEmotionStyle(text: String, emotion: AppState.Emotion): String {
        return when (emotion) {
            AppState.Emotion.SENANG -> "😊 $text"
            AppState.Emotion.CAPEK -> "😴 $text"
            AppState.Emotion.SEDIH -> "😔 $text"
            AppState.Emotion.MARAH -> "😡 $text"
            else -> text
        }
    }

    /**
     * Placeholder simulasi TTS (nanti bisa ganti neural TTS)
     */
    private suspend fun simulateSpeechOutput(text: String) {
        val words = text.split(" ")
        for (word in words) {
            println("[VoiceService] $word") // debug console, nanti ganti TTS engine
            delay(150L + Random.nextLong(0, 100)) // variasi tempo manusia
        }
    }

    fun stop() {
        speakJob?.cancel()
    }
}