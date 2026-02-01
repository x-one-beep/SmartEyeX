package com.smarteyex.core

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class WaNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn?.packageName == "com.whatsapp") {
            val notificationText = sbn.notification.extras.getString("android.text") ?: ""
            // Baca notifikasi dan trigger TTS
            VoiceEngine(applicationContext).speakNotification(notificationText)
            // Trigger auto reply
            WaReplyManager().autoReply("Auto reply to: $notificationText")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Handle removal
    }
}