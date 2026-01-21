package com.smarteyex.core

import android.content.Context
import com.smarteyex.core.ai.GroqAIEngine
import com.smarteyex.core.tts.TextToSpeechManager
import com.smarteyex.core.wa.WaSessionManager

object VoiceRouter {

    fun route(context: Context, spoken: String) {
        val lower = spoken.lowercase()

        when {
            lower.contains("jawab") ||
            lower.contains("read") ||
            lower.contains("diemin") -> {
                WaSessionManager.handleVoiceCommand(context, spoken)
            }

            else -> {
                // fallback → chat AI
                val ai = GroqAIEngine(context)
                val tts = TextToSpeechManager(context)

                val response = ai.chat(spoken)
                tts.speak(response)
            }
        }
    }
}
