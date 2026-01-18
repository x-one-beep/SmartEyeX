package com.smarteyex.core.wa

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.smarteyex.core.data.AppDatabase
import com.smarteyex.core.data.Event
import com.smarteyex.core.tts.TextToSpeechManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WaListenerService : NotificationListenerService() {

    private lateinit var tts: TextToSpeechManager

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeechManager(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val packageName = sbn.packageName
        if (packageName != "com.whatsapp") return

        val extras = sbn.notification.extras
        val sender = extras.getString("android.title") ?: "Unknown"
        val message = extras.getCharSequence("android.text")?.toString() ?: return

        Log.d("SmartEyeX-WA", "WA dari $sender: $message")

        // 🔊 SPEAK
        tts.speak("Bung, pesan WhatsApp dari $sender. Isinya: $message")

        // 🧠 SAVE MEMORY
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(applicationContext)
                .eventDao()
                .insert(
                    Event(
                        time = System.currentTimeMillis(),
                        type = "WHATSAPP",
                        data = "From $sender: $message"
                    )
                )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }
}
