package com.smarteyex.core

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class VoiceService : Service() {

    private lateinit var voiceEngine: VoiceEngine

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        startForeground(101, buildNotification())

        voiceEngine = VoiceEngine(this) { spokenText ->
            SpeechCommandProcessor.process(spokenText)
        }

        voiceEngine.start()
    }

    override fun onDestroy() {
        voiceEngine.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SmartEyeX aktif")
            .setContentText("Gue dengerin lu di background")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SmartEyeX Voice Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Service suara SmartEyeX"
                setSound(null, null)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "SMART_EYEX_VOICE"
    }
}