package com.smarteyex.core

class NavigationStateManager {

    // Track state navigasi UI (e.g., panel aktif)
    private var currentPanel: String = "dashboard"  // dashboard, ai_chat, wa_panel, etc.

    // Fungsi untuk switch panel
    fun switchToPanel(panel: String) {
        currentPanel = panel
        // Logika untuk update UI (e.g., show/hide fragments)
    }

    // Getter untuk panel aktif
    fun getCurrentPanel(): String = currentPanel
}