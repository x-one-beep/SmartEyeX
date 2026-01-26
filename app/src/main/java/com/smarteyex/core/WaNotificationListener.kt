package com.smarteyex.core.wa

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.os.Bundle
import com.smarteyex.core.VoiceEngine
import com.smarteyex.core.memory.MemoryManager

class WaNotificationListener : NotificationListenerService() {

    private lateinit var voice: VoiceEngine
    private lateinit var memory: MemoryManager
    private lateinit var autoReply: WaAutoReplyManager

    override fun onCreate() {
        super.onCreate()
        voice = VoiceEngine(this)
        memory = MemoryManager(this)
        autoReply = WaAutoReplyManager(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!sbn.packageName.contains("com.whatsapp")) return

        val extras: Bundle = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        // Simpan ke memori
        memory.save("wa_incoming", "$title: $text")

        // Bacakan via suara
        voice.speak("Pesan WhatsApp dari $title. Isinya $text")

        // Auto reply logic
        autoReply.replyIfNeeded(sbn, title, text)
    }
}