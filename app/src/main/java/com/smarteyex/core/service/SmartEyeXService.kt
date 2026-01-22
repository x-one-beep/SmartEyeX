package com.smarteyex.core

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.Build
import androidx.core.app.NotificationCompat
import com.smarteyex.core.MainActivity
import com.smarteyex.app.R

class SmartEyeXService : Service() {

    companion object {
        const val CHANNEL_ID = "SmartEyeX_BG_CHANNEL"
        const val NOTIF_ID = 130809
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 🔒 DI SINI NANTI:
        // - Listener WA aktif
        // - Voice trigger aktif
        // - AI background logic
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Matikan semua listener background di sini
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SmartEyeX Aktif")
            .setContentText("Background mode berjalan")
            .setSmallIcon(R.drawable.ic_notification) // icon kecil
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SmartEyeX Background",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
