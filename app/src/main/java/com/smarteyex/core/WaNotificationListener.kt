package com.smarteyex.core.wa

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.smarteyex.core.VoiceService

class WaNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!sbn.packageName.contains("com.whatsapp")) return

        val extras = sbn.notification.extras
        val sender = extras.getString(Notification.EXTRA_TITLE) ?: return
        val message = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        val intent = Intent(this, VoiceService::class.java).apply {
            action = "WA_MESSAGE"
            putExtra("sbn", sbn)
            putExtra("sender", sender)
            putExtra("message", message)
        }

        startService(intent)
    }
}