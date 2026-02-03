package com.smarteyex.core.ai

import com.smarteyex.core.state.AppState
import com.smarteyex.core.memory.MemoryManager
import com.smarteyex.core.voice.VoiceResponse
import kotlin.random.Random

class GroqAiEngine(private val appState: AppState, private val memoryManager: MemoryManager) {

    /**
     * Generate live response untuk SmartEyeXService
     */
    fun generateLiveResponse(
        speech: String,
        emotion: AppState.Emotion,
        context: AppState.Context
    ): VoiceResponse {

        // ===============================
        // Ambil memory dan konteks
        // ===============================
        val recentMemory = memoryManager.getRecentMemory()
        val userMood = appState.currentEmotion

        // ===============================
        // Tentuin personality & tone
        // ===============================
        val baseResponses = listOf(
            "Oh gitu ya… menarik juga!",
            "Hmm, gue ngerti kok.",
            "Haha, iya sih…",
            "Wah, keren tuh!"
        )

        // ===============================
        // Variasi sesuai mood user
        // ===============================
        val finalText = when (userMood) {
            AppState.Emotion.SENANG -> baseResponses.random() + " Seneng banget liat kamu happy!"
            AppState.Emotion.CAPEK -> "Ahh, capek ya… santai dulu, jangan dipaksain."
            AppState.Emotion.SEDIH -> "Duh, gue ngerti perasaan lo… tetap kuat ya."
            AppState.Emotion.MARAH -> "Tenang dulu ya… jangan buru-buru ambil keputusan."
            else -> baseResponses.random()
        }

        // ===============================
        // Bisa nambahin nimbrung sosial / gen-Z style nanti
        // ===============================
        return VoiceResponse(
            text = finalText,
            emotion = userMood,
            shouldSpeak = true
        )
    }
}