package com.smarteyex.core

import com.smarteyex.core.ai.GroqAiEngine

/**
 * WAReplyManager
 * Handle logika reply WA sesuai self-restraint & konteks
 */
class WAReplyManager(private val aiEngine: GroqAiEngine) {

    fun generateReply(notifText: String): String {
        // Gunakan GroqAI untuk rekomendasi balasan
        return aiEngine.generateNotificationReply(notifText)
    }

    fun shouldReply(userBusy: Boolean, emotion: com.smarteyex.core.state.AppState.Emotion): Boolean {
        // Self-restraint logic WA
        if (userBusy) return false
        if (emotion.isOverwhelmed()) return false
        return true
    }
}