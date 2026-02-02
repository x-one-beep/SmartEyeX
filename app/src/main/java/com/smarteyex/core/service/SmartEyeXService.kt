package com.smarteyex.core.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import com.smarteyex.core.ClockManager

class SmartEyeXService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification())
        ClockManager.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ServiceController.startAll(this)
        return START_STICKY
    }

    override fun onDestroy() {
        ClockManager.stop()
        ServiceController.stopAll()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelId = "smarteyex_core"
        val channel = NotificationChannel(
            channelId,
            "SmartEyeX AI",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return Notification.Builder(this, channelId)
            .setContentTitle("SmartEyeX aktif")
            .setContentText("AI lagi hidup di background")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }
}