package com.smarteyex.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.smarteyex.core.*

class SmartEyeXService : Service() {

    private lateinit var voiceEngine: VoiceEngine
    private lateinit var speechCommandProcessor: SpeechCommandProcessor
    private lateinit var delayManager: DelayManager

    override fun onCreate() {
        super.onCreate()
        voiceEngine = VoiceEngine(this)
        speechCommandProcessor = SpeechCommandProcessor(this)
        delayManager = DelayManager()

        // Buat notification channel untuk foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("SmartEyeX", "SmartEyeX Service", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Start foreground untuk hindari kill
        val notification: Notification = NotificationCompat.Builder(this, "SmartEyeX")
            .setContentTitle("SmartEyeX Running")
            .setContentText("AI and voice services active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        startForeground(1, notification)

        // Start listening perintah suara di background
        speechCommandProcessor.startListening { command ->
            // Process command dan trigger AI
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Jalankan service di background tanpa drain baterai berlebihan
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        speechCommandProcessor.stopListening()
        voiceEngine.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, SmartEyeXService::class.java)
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, SmartEyeXService::class.java)
            context.stopService(intent)
        }
    }
}