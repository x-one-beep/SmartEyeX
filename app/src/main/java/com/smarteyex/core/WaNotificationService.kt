package com.smarteyex.core.service

import android.app.Notification
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityEvent
import android.accessibilityservice.AccessibilityService
import com.smarteyex.core.AppState
import com.smarteyex.core.voice.VoiceEngine
import com.smarteyex.core.memory.WaMessageBuffer

/**
 * Service ini menangkap semua notifikasi WA
 * dan langsung trigger AI untuk baca & offer reply.
 */
class WaNotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Hanya WA
        if (!sbn.packageName.contains("whatsapp")) return

        val extras = sbn.notification.extras
        val sender = extras.getString(Notification.EXTRA_TITLE) ?: return
        val message = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        // Simpan ke buffer memory WA
        WaMessageBuffer.store(
            sender = sender,
            message = message,
            time = System.currentTimeMillis()
        )

        // Update AppState
        AppState.lastWaNotification = sbn

        // Check busy mode
        if (!AppState.isBusy.get()) {
            // Trigger suara AI (voice engine)
            VoiceEngine.speak(
                "Ada pesan dari $sender. ${message.take(80)}"
            )
        }
    }
}

/**
 * Accessibility Service untuk kirim reply WA
 * atas nama user. Ini legal & aman.
 */
class WaReplyAccessibilityService : AccessibilityService() {

    companion object {
        var instance: WaReplyAccessibilityService? = null

        fun sendReply(text: String) {
            instance?.replyToLatest(text)
        }
    }

    override fun onServiceConnected() {
        instance = this
    }

    private fun replyToLatest(text: String) {
        val root = rootInActiveWindow ?: return

        // Cari tombol "Reply"
        val replyButton = root.findAccessibilityNodeInfosByText("Reply")
            .firstOrNull() ?: return

        replyButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)

        // Tunggu UI ready, set text
        Handler(Looper.getMainLooper()).postDelayed({
            val input = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            val args = Bundle()
            args.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
            input?.performAction(
                AccessibilityNodeInfo.ACTION_SET_TEXT,
                args
            )
            // Kirim text
            input?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }, 300)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}