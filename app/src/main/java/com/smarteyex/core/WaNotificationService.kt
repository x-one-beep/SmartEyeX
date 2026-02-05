package com.smarteyex.core.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.smarteyex.core.voice.SpeechOutput
import com.smarteyex.core.AppState

class WaNotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!sbn.packageName.contains("whatsapp")) return

        val extras = sbn.notification.extras
        val sender = extras.getString(Notification.EXTRA_TITLE) ?: return
        val message = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        AppState.lastWaNotification = sbn

        if (!AppState.isBusy.get()) {
            SpeechOutput.speak(
                "ada pesan dari $sender. dia bilang $message"
            )
        }
    }
}