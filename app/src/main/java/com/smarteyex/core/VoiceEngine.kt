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
                val speech = simulateUserSpeech()
                if (speech.isNotEmpty()) listeners.forEach { it(speech) }
                delay(300L)
            }
        }
    }

    fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
    }

    fun setTriggerWord(word: String) { triggerWord = word.lowercase() }
    fun containsTrigger(speech: String) = speech.lowercase().contains(triggerWord)

    fun adjustSensitivity(emotion: AppState.Emotion, mode: AppState.Mode, batteryLow: Boolean) {
        // bisa implement sensor adaptif → hemat daya, adaptif mood
    }

    private fun simulateUserSpeech(): String {
        val samples = listOf("halo", "lagi capek nih", "senang banget hari ini", "eh liat ini", "ada notif penting ga")
        return if (Random.nextBoolean()) samples.random() else ""
    }
}