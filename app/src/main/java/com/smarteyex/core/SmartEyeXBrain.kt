package com.smarteyex.core

import com.smarteyex.core.voice.VoiceEngine
import com.smarteyex.core.service.WaReplyAccessibilityService

object SmartEyeXBrain {

    /**
     * Dipanggil saat notif WA masuk
     */
    fun onWaMessageReceived(sender: String, message: String) {
        if (AppState.isBusyMode) return

        if (PriorityResolver.isHighPriority(sender, message)) {
            // Baca pesan dengan voice AI
            VoiceEngine.speak(
                "Ada pesan dari $sender. ${message.take(80)}"
            )
        }
    }

    /**
     * Dipanggil saat user ngomong jawaban
     */
    fun onUserVoiceReply(text: String) {
        // Kirim reply atas nama user via AccessibilityService
        WaReplyAccessibilityService.sendReply(text)
    }
}