package com.smarteyex.core

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.smarteyex.core.R

class VoiceService : Service() {

    private lateinit var voiceEngine: VoiceEngine

    override fun onCreate() {
        super.onCreate()

        voiceEngine = VoiceEngine(this) { spoken ->
            VoiceRouter.route(this, spoken)
        }

        startForeground(130809, buildNotif())
        voiceEngine.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceEngine.destroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotif(): Notification {
        return NotificationCompat.Builder(this, "smart_eye_channel")
            .setContentTitle("SmartEyeX Active")
            .setContentText("Voice & AI running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }
}
