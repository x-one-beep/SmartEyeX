package com.smarteyex.core

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.smarteyex.app.R

class CoreService : Service() {

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        CoreController.start(this)
    }

    override fun onDestroy() {
        CoreController.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startAsForeground() {
        val channelId = "smarteyex_core"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SmartEyeX Core",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SmartEyeX aktif")
            .setContentText("Gue di sini, nemenin lo.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }
}