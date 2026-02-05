package com.smarteyex.core.voice

import com.smarteyex.core.AppState

object EmotionDetector {

    fun detect(text: String): AppState.Emotion {
        return when {
            text.contains("capek", true) -> AppState.Emotion.TIRED
            text.contains("sedih", true) -> AppState.Emotion.SAD
            text.contains("kesel", true) -> AppState.Emotion.ANGRY
            text.isBlank() -> AppState.Emotion.EMPTY
            else -> AppState.Emotion.NEUTRAL
        }
    }
}