package com.smarteyex.core

object GroqAiEngine {

    fun chat(
        input: String,
        topic: String?,
        emotion: AppState.Emotion
    ): String {

        val style = when (emotion) {
            AppState.Emotion.CAPEK -> "jawab santai, agak males tapi tetep peduli"
            AppState.Emotion.KESEL -> "jawab ketus dikit tapi masih gen-z"
            AppState.Emotion.SENENG -> "jawab rame, santai"
            AppState.Emotion.EMPATI -> "jawab lembut dan ngerti perasaan"
            else -> "jawab santai gen-z"
        }

        // NOTE:
        // API KEY SUDAH ADA DI SECRET VARIABLE
        // Di sini diasumsikan request ke Groq dilakukan

        return "Hmm… menurut gue sih $input, tapi ya tergantung lu juga sih. Santai aja."
    }
}