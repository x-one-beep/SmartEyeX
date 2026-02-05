package com.smarteyex.notification

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.accessibilityservice.AccessibilityService
import com.smarteyex.core.AppState
import com.smarteyex.core.voice.VoiceEngine
import com.smarteyex.core.memory.WaMessageBuffer

/**
 * 1️⃣ Notification Listener → baca WA
 */
class WaNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!sbn.packageName.contains("whatsapp")) return

        val extras = sbn.notification.extras
        val sender = extras.getString(Notification.EXTRA_TITLE) ?: return
        val message = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        // Simpan ke memory WA
        WaMessageBuffer.store(sender, message, System.currentTimeMillis())
        AppState.lastWaNotification = sbn

        if (!AppState.isBusy.get()) {
            VoiceEngine.speak("Ada pesan dari $sender: ${message.take(80)}")
        }
    }
}

/**
 * 2️⃣ WA reply sender pakai RemoteInput
 */
object WhatsAppReplySender {

    fun sendReply(sbn: StatusBarNotification, reply: String) {
        val notification = sbn.notification
        val actions = notification.actions ?: return

        for (action in actions) {
            if (action.remoteInputs != null) {
                val intent = Intent()
                val bundle = Bundle()

                for (input in action.remoteInputs) {
                    bundle.putCharSequence(input.resultKey, reply)
                }

                RemoteInput.addResultsToIntent(action.remoteInputs, intent, bundle)

                try {
                    action.actionIntent.send(null, 0, intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

/**
 * 3️⃣ Accessibility fallback → kirim WA atas nama user
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
        val replyButton = root.findAccessibilityNodeInfosByText("Reply")
            .firstOrNull() ?: return

        replyButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)

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
            input?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }, 300)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}

/**
 * 4️⃣ SmartEyeX Brain → hub WA + voice
 */
object SmartEyeXBrain {

    fun onWaMessageReceived(sender: String, message: String, sbn: StatusBarNotification) {
        if (AppState.isBusy.get()) return

        // Voice baca WA
        VoiceEngine.speak("Pesan dari $sender: ${message.take(80)}")

        // Bisa nunggu user reply voice → nanti kirim WA
    }

    fun onUserVoiceReply(sbn: StatusBarNotification, text: String) {
        // Coba RemoteInput dulu
        WhatsAppReplySender.sendReply(sbn, text)

        // Fallback Accessibility
        WaReplyAccessibilityService.sendReply(text)
    }
}