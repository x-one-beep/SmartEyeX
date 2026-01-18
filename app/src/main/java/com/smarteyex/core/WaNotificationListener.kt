package com.smarteyex.core.wa

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.content.Context
import android.util.Log
import java.util.*

class WaNotificationListener : NotificationListenerService() {

    private var tts: TextToSpeech? = null

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale("id", "ID")
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pack = sbn.packageName
        if (pack.contains("com.whatsapp")) {
            val msg = sbn.notification.extras.getString("android.text") ?: "WA masuk"
            speakWA(msg)
        }
    }

    private fun speakWA(text: String) {
        tts?.speak("Bung X, ada WA: $text", TextToSpeech.QUEUE_ADD, null, null)
        Log.d("WA_LISTENER", text)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.shutdown()
    }
}
