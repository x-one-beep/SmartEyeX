package com.smarteyex.core

class AppState {

    // Track state aplikasi (e.g., mode AI aktif)
    var isAIModeActive: Boolean = false
    var currentMode: String = "idle"  // idle, chat, wa_reply, etc.

    // Fungsi untuk update state
    fun setMode(mode: String) {
        currentMode = mode
        isAIModeActive = mode != "idle"
    }

    // Getter untuk state
    fun getCurrentMode(): String = currentMode
}