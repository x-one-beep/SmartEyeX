package com.smarteyex.core

import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * SpeechCommand
 * Tangkap perintah suara dari user
 */
class SpeechCommand {

    private var listeningJob: Job? = null
    private val commandCallbacks = mutableMapOf<String, () -> Unit>()

    fun registerCommand(command: String, callback: () -> Unit) {
        commandCallbacks[command.lowercase()] = callback
    }

    fun startListening() {
        listeningJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val speech = simulateUserSpeech()
                if (speech.isNotEmpty()) {
                    commandCallbacks[speech.lowercase()]?.invoke()
                }
                delay(500L)
            }
        }
    }

    fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
    }

    private fun simulateUserSpeech(): String {
        val possible = listOf("halo smart", "buka chat", "tutup camera", "tunjukin memory")
        return if (Random.nextBoolean()) possible.random() else ""
    }
}