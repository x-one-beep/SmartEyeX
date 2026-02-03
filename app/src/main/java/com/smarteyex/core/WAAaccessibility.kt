package com.smarteyex.core

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WAAccessibility : AccessibilityService() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    var onMessageReceived: ((WAIncomingMessage) -> Unit)? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Cek notif WA
        if (event.packageName == "com.whatsapp" && event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            val msgText = event.text?.joinToString(" ") ?: return
            val sender = event.contentDescription?.toString() ?: "Unknown"

            val incoming = WAIncomingMessage(sender, msgText)
            coroutineScope.launch {
                onMessageReceived?.invoke(incoming)
            }
        }
    }

    override fun onInterrupt() {
        // Tidak perlu
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Toast.makeText(this, "WA Accessibility Ready", Toast.LENGTH_SHORT).show()
    }

    fun sendMessage(number: String, text: String) {
        // Placeholder: integrasi nanti dengan Accessibility API
        // Bisa pakai Intent ACTION_SEND atau Accessibility
        Toast.makeText(this, "Send WA to $number: $text", Toast.LENGTH_SHORT).show()
    }
}

data class WAIncomingMessage(
    val sender: String,
    val message: String
)