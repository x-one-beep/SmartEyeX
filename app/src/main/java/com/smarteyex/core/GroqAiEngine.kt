package com.smarteyex.core.ai

import com.smarteyex.core.memory.MemoryManager
import com.smarteyex.core.state.AppState
import com.smarteyex.core.voice.VoiceResponse
import kotlin.random.Random

class GroqAiEngine(private val appState: AppState, private val memoryManager: MemoryManager) {

    /**
     * Generate response untuk live conversation di SmartEyeXService
     */
    fun generateLiveResponse(
        speech: String,
        emotion: AppState.Emotion,
        context: AppState.Context
    ): VoiceResponse {

        val recentMemory = memoryManager.getRecentMemory()
        val userMood = appState.currentEmotion

        // Base response dengan variasi
        val baseResponses = listOf(
            "Oh gitu ya… menarik juga!",
            "Hmm, gue ngerti kok.",
            "Haha, iya sih…",
            "Wah, keren tuh!"
        )

        // Modifikasi respons sesuai emosi user
        val finalText = when (userMood) {
            AppState.Emotion.SENANG -> baseResponses.random() + " Seneng banget liat kamu happy!"
            AppState.Emotion.CAPEK -> "Ahh, capek ya… santai dulu, jangan dipaksain."
            AppState.Emotion.SEDIH -> "Duh, gue ngerti perasaan lo… tetap kuat ya."
            AppState.Emotion.MARAH -> "Tenang dulu ya… jangan buru-buru ambil keputusan."
            else -> baseResponses.random()
        }

        return VoiceResponse(
            text = finalText,
            emotion = userMood,
            shouldSpeak = true
        )
    }

    /**
     * Generate response untuk notifikasi (WA / sistem)
     */
    fun generateNotificationReply(notifText: String): String {
        val templates = listOf(
            "Mau gue bantu balas pesan ini?",
            "Lo pengen gue tanggepin notif ini?",
            "Boleh gue bantu bikin reply?",
            "Ini penting, mau gue balas?"
        )
        return templates.random()
    }
}