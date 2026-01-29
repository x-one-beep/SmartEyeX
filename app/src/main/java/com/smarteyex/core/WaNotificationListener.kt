package com.smarteyex.core.wa

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent
import com.smarteyex.core.VoiceService

class WaNotificationListener : NotificationListenerService() {

    private lateinit var waReply: WaReplyManager

    override fun onCreate() {
        super.onCreate()
        waReply = WaReplyManager(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        if (sbn.packageName != "com.whatsapp") return

        val extras = sbn.notification.extras

        val sender =
            extras.getCharSequence("android.title")?.toString() ?: return

        val message =
            extras.getCharSequence("android.text")?.toString() ?: return

        waReply.readIncoming(sender, message)
    }
}