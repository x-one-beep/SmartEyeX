package com.smarteyex.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

class WAReplyManager(
    private val appState: AppState,
    private val aiEngine: GroqAiEngine
) {

    private val queue = ConcurrentLinkedQueue<WAIncomingMessage>()
    private val scope = CoroutineScope(Dispatchers.Default)

    fun queueNotification(notif: WAIncomingMessage) {
        queue.add(notif)
        processQueue()
    }

    private fun processQueue() {
        scope.launch {
            while (queue.isNotEmpty()) {
                val notif = queue.poll() ?: continue

                // Cek self-restraint
                if (appState.isUserBusy || appState.currentEmotion.isOverwhelmed() || appState.isAIResting) continue

                // Generate response AI
                val response = aiEngine.generateLiveResponse(
                    speech = notif.message,
                    emotion = appState.currentEmotion,
                    context = appState.currentContext
                )

                if (!response.shouldSpeak) continue

                // Tanyain user via VoiceService (SmartEyeXService sudah handle)
                // Jika user setuju → kirim WA
                WAAccessibilityServiceSingleton.sendMessage(notif.sender, response.text)
            }
        }
    }
}

// Singleton untuk akses WAAccessibility dari mana saja
object WAAccessibilityServiceSingleton {
    private var service: WAAccessibility? = null

    fun register(service: WAAccessibility) {
        this.service = service
    }

    fun sendMessage(number: String, text: String) {
        service?.sendMessage(number, text)
    }
}