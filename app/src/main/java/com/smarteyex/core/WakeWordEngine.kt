package com.smarteyex.core.voice

object WakeWordEngine {

    private var listening = true

    fun setListening(enabled: Boolean) {
        listening = enabled
    }

    fun detectWakeWord(audio: ByteArray): Boolean {
        if (!listening) return false
        // Analisis audio → return true jika wake word
        return false
    }
}