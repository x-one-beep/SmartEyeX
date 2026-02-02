package com.smarteyex.core

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class WaNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!sbn.packageName.contains("whatsapp")) return

        val extras = sbn.notification.extras
        val sender = extras.getString("android.title") ?: return
        val text = extras.getCharSequence("android.text")?.toString() ?: return

        // Queue biar gak nyela
        AppState.enqueueTask {
            AppSpeak.say(
                "Ada pesan dari $sender. Isinya, $text. Mau gue jawab?"
            )
            WaReplyManager.prepareReply(sender)
        }
    }
}