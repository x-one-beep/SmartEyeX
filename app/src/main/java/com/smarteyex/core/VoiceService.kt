package com.smarteyex.core.voice

import android.content.Context
import com.smarteyex.core.state.AppState
import kotlinx.coroutines.*

class VoiceService(private val context: Context, private val appState: AppState) {

    private var speakingJob: Job? = null

    fun speak(text: String, emotion: AppState.Emotion) {
        speakingJob?.cancel()
        speakingJob = CoroutineScope(Dispatchers.Default).launch {
            val delayMs = (300L..700L).random()
            delay(delayMs)
            // TODO: implement TTS / Neural Voice output sesuai emotion
            println("[AI Voice][$emotion]: $text") // placeholder debug
        }
    }

    fun setSchoolMode(enabled: Boolean) {
        // tweak voice behavior → lebih low power / lebih sopan
    }

    fun setGameMode(enabled: Boolean) {
        // tweak voice behavior → lebih cepat, energetic
    }

    fun setAlwaysListening(enabled: Boolean) {
        // toggle VoiceEngine listening
    }

    fun setRestingMode(enabled: Boolean) {
        // AI voice istirahat → tidak bicara sementara
    }

    fun setEmotionLevel(level: Int) {
        // tweak prosody / intonasi TTS
    }
}