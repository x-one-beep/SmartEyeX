package com.smarteyex.core.wa

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import com.smarteyex.core.VoiceEngine

class WaNotificationListener : NotificationListenerService() {

    private lateinit var voice: VoiceEngine

    override fun onNotificationPosted(sbn: StatusBarNotification) {
    if (!sbn.packageName.contains("com.whatsapp")) return

    val extras = sbn.notification.extras
    val sender = extras.getString(Notification.EXTRA_TITLE) ?: return
    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

    val intent = Intent(this, VoiceService::class.java).apply {
        action = "WA_MESSAGE"
        putExtra("sender", sender)
        putExtra("message", text)
    }

    startService(intent)
}