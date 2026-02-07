package com.smarteyex.core

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class BrainForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()

        AppContextHolder.context = applicationContext

        startForeground(
            NOTIF_ID,
            buildNotification()
        )

        // 🧠 BOOT SEMUA OTAK
        SpeechOutput.init(applicationContext)
        SensorBrainIntegrator.init(applicationContext)
        SmartDashboard.init(applicationContext)

        // 🔥 optional: langsung listen
        // VoiceInputController(applicationContext).startListening { text ->
        //     MultiModalEngine.handleVoiceInput(text)
        // }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        return START_STICKY // 🧠 HIDUP TERUS
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelId = "smarteyex_brain"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SmartEyeX Brain",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("SmartEyeX aktif")
            .setContentText("AI berjalan di background")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val NOTIF_ID = 777
    }
}