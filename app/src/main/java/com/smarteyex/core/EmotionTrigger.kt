package com.smarteyex.emotion

object EmotionTrigger {

    fun onUserSilent(hours: Int) {
        if (hours > 12) {
            EmotionEngine.update(
                mood = "kangen",
                intensity = 7,
                reason = "lama gak ngobrol"
            )
        }
    }

    fun onUserSad() {
        EmotionEngine.update(
            mood = "khawatir",
            intensity = 9,
            reason = "user sedih"
        )
    }

    fun onUserHappy() {
        EmotionEngine.update(
            mood = "senang",
            intensity = 8,
            reason = "user senang"
        )
    }
}