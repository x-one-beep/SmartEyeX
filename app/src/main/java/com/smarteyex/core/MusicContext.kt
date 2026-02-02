package com.smarteyex.core

object MusicContext {

    fun playForMood(emotion: AppState.Emotion) {
        val vibe = when (emotion) {
            AppState.Emotion.SEDIH -> "lagu galau"
            AppState.Emotion.SENENG -> "lagu upbeat"
            else -> "lagu santai"
        }
        AppSpeak.say("Gue setelin $vibe ya.")
        // Integrasi player/web bisa ditambah di sini
    }
}