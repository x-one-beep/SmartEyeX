package com.smarteyex.core

object AppState {
    @Volatile
    var isSpeaking: Boolean = false  // track AI / WA TTS lagi jalan
}