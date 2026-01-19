package com.smarteyex.app.wa

import android.app.Notification
import android.app.Notification.Action
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.smarteyex.app.ai.GroqAI
import com.smarteyex.app.memory.MemoryManager
import com.smarteyex.app.voice.VoiceEngine

class WaNotificationListener : NotificationListenerService() {

    private lateinit var memoryManager: MemoryManager
    private lateinit var ai: GroqAI
    private lateinit var voice: VoiceEngine

    override fun onCreate() {
        super.onCreate()

        memoryManager = MemoryManager(this)
        ai = GroqAI(BuildConfig.GROQ_API_KEY)
        voice = VoiceEngine(this) {}
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        if (sbn.packageName != "com.whatsapp") return

        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        // Filter notifikasi sistem WA
        if (title.contains("WhatsApp", true)) return

        Log.d("SmartEyeX-WA", "Dari: $title | Pesan: $text")

        // Simpan ke memori
        memoryManager.save("WA dari $title: $text")

        // Proses AI
        Thread {
            val response = ai.ask(
                "Balas pesan WhatsApp secara sopan, singkat, gaya Gen Z. Pesan: $text"
            )

            // Balas otomatis
            reply(notification.actions, response)

            // Suarakan
            voice.speak("Pesan dari $title. Saya sudah membalas.")
        }.start()
    }

    private fun reply(actions: Array<Action>?, message: String) {
        if (actions == null) return

        for (action in actions) {
            if (action.remoteInputs != null) {
                WaReplyHelper.sendReply(
                    applicationContext,
                    action,
                    message
                )
                break
            }
        }
    }
}
