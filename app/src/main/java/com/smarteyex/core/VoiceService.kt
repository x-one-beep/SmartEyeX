package com.smarteyex.core

import android.app.*
import android.content.Intent
import android.os.*
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import com.smarteyex.core.wa.WaReplyManager

class VoiceService : Service() {

    private lateinit var recognizer: SpeechRecognizer
    private lateinit var voice: VoiceEngine
    private lateinit var waReplyManager: WaReplyManager

    override fun onCreate() {
        super.onCreate()

        startForeground(1, buildNotification())

        voice = VoiceEngine(this)
        voice.init()

        waReplyManager = WaReplyManager()

        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {}
            override fun onError(error: Int) {}
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    override fun onBind(intent: Intent?) = null

    private fun buildNotification(): Notification {
        val channelId = "SMART_EYE_X"
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "SmartEyeX Voice", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("SmartEyeX aktif")
            .setContentText("Mendengarkan Bung Smart")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now) // ganti drawable R
            .build()
    }

    override fun onDestroy() {
        recognizer.destroy()
        voice.shutdown()
        super.onDestroy()
    }
}