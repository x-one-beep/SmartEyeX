package com.smarteyex.core.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(entities = [Event::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
 abstract fun eventDao(): EventDao
companion object {
    private const val DB_NAME = "smartey_db"
    @Volatile private var instance: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            val i = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
               marteyex/core/service/DetectForegroundService.kt <<'EOF'
package com.smarteyex.core.service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.smarteyex.core.R
class DetectForegroundService : Service() {
companion object {
    const val CHANNEL_ID = "smarteyx_foreground"
    const val NOTIF_ID = 101
}

override fun onCreate() {
    super.onCreate()
    createChannel()
    val notif: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("SmartEyeX")
        .setContentText("Guard active")
        .setSmallIcon(R.mipmap.ic_launcher)
        .setOngoing(true)
        .build()
    startForeground(NOTIF_ID, notif)
}

override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    // TODO: start background detection task / WorkManager if needed.
    // This service currently keeps the app in foreground to allow background camera/mic work if implemented.
    return START_STICKY
}

override fun onDestroy() {
    super.onDestroy()
    stopForeground(true)
}

override fun onBind(intent: Intent?): IBinder? = null

private fun createChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val nm = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(CHANNEL_ID, "SmartEyeX Foreground", NotificationManager.IMPORTANCE_LOW)
        nm.createNotificationChannel(channel)
    }
}
}
