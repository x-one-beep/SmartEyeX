package com.smarteyex.core.voice

import com.smarteyex.core.state.AppState
import kotlinx.coroutines.*
import kotlin.random.Random

class VoiceEngine(private val appState: AppState) {

    private var listeningJob: Job? = null
    private var triggerWord: String = "hey smart"

    private val listeners = mutableListOf<(String) -> Unit>()

    fun startListening(callback: (speech: String) -> Unit) {
        listeners.add(callback)
        if (listeningJob?.isActive == true) return

        listeningJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                if (!appState.isMicAllowed()) {
                    delay(500)
                    continue
                }

                val speech = simulateUserSpeech() // placeholder, nanti pakai STT asli

                if (speech.isNotEmpty()) {
                    listeners.forEach { it(speech) }
                }

                delay(300L)
            }
        }
    }

    fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
    }

    fun setTriggerWord(word: String) {
        triggerWord = word.lowercase()
    }

    fun containsTrigger(speech: String): Boolean {
        return speech.lowercase().contains(triggerWord)
    }

    fun adjustSensitivity(
        emotion: AppState.Emotion,
        mode: AppState.Mode,
        batteryLow: Boolean
    ) {
        // jika user capek / mode sekolah → kurangi frekuensi listening
        // jika battery low → kurangi sampling rate
        // ini untuk hemat daya
    }

    private fun simulateUserSpeech(): String {
        // Hanya simulasi; nanti pakai STT
        val possible = listOf(
            "halo", "lagi capek nih", "senang banget hari ini",
            "eh liat ini", "ada notif penting ga"
        )
        return if (Random.nextBoolean()) possible.random() else ""
    }
}