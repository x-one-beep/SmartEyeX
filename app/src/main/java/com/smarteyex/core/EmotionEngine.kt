package com.smarteyex.emotion

object EmotionEngine {

    private var current = EmotionState(
        mood = "netral",
        intensity = 5,
        reason = "default"
    )

    fun update(mood: String, intensity: Int, reason: String) {
        current = EmotionState(mood, intensity.coerceIn(1,10), reason)
    }

    fun get(): EmotionState = current
}