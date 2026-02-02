package com.smarteyex.core

import android.content.Context
import kotlinx.coroutines.*

class VoiceService(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start() {
        AppSpeak.init(context) { text ->
            AppState.updateActivity()
            SpeechCommandProcessor.process(text)
        }

        scope.launch {
            while (isActive) {
                if (!AppState.isSpeaking && !AppState.isListening) {
                    AppSpeak.listen()
                }
                delay(1500)
            }
        }
    }

    fun stop() {
        scope.cancel()
        AppSpeak.destroy()
    }
}
