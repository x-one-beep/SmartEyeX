package com.smarteyex.core.voice

import com.smarteyex.core.state.AppState
import kotlinx.coroutines.*
import kotlin.random.Random

data class VoiceResponse(
    val text: String,
    val emotion: AppState.Emotion,
    val shouldSpeak: Boolean = true
)

class VoiceService(private val appState: AppState) {

    private var speakJob: Job? = null

    fun speak(text: String, emotion: AppState.Emotion) {
        if (!appState.isMicAllowed() || appState.isConversationCrowded) return

        speakJob?.cancel()
        speakJob = CoroutineScope(Dispatchers.IO).launch {
            val finalText = applyEmotionStyle(text, emotion)
            simulateSpeechOutput(finalText)
        }
    }

    private fun applyEmotionStyle(text: String, emotion: AppState.Emotion): String {
        return when(emotion) {
            AppState.Emotion.SENANG -> "😊 $text"
            AppState.Emotion.CAPEK -> "😴 $text"
            AppState.Emotion.SEDIH -> "😔 $text"
            AppState.Emotion.MARAH -> "😡 $text"
            else -> text
        }
    }

    private suspend fun simulateSpeechOutput(text: String) {
        val words = text.split(" ")
        for(word in words) {
            println("[VoiceService] $word") // debug, nanti ganti neural TTS
            delay(150L + Random.nextLong(0,100)) // tempo manusia
        }
    }

    fun stop() { speakJob?.cancel() }
}
