package com.smarteyex.notification

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.smarteyex.voice.*

class WhatsAppNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        if (sbn.packageName != "com.whatsapp") return

        val notification = sbn.notification
        val extras = notification.extras

        val sender = extras.getString(Notification.EXTRA_TITLE) ?: return
        val message = extras.getString(Notification.EXTRA_TEXT) ?: return

        // 1️⃣ Bacain WA
        SmartVoiceEngine().speak(
            text = "$sender bilang, $message",
            emotion = VoiceEmotion.CALM,
            intent = SpeechIntent.RESPOND
        )

        // 2️⃣ Tanyain user
        SmartVoiceEngine().speak(
            text = "mau dijawab, dibaca aja, atau diemin?",
            emotion = VoiceEmotion.CARING,
            intent = SpeechIntent.CASUAL_CHAT
        )

        // selanjutnya: nunggu jawaban suara
        PendingReplyCache.store(sbn)
    }
}