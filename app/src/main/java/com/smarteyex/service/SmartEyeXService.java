package com.smarteyex.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import com.smarteyex.ai.AudioEngine;
import com.smarteyex.ai.CameraVisionEngine;
import com.smarteyex.ai.MemoryEngine;

public class SmartEyeXService extends Service {
    private PowerManager.WakeLock wakeLock;
    private AudioEngine audioEngine;
    private CameraVisionEngine cameraEngine;
    private MemoryEngine memoryEngine;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            createNotificationChannel();
            startForeground(1, buildNotification());
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SmartEyeX:WakeLock");
            wakeLock.acquire();
            audioEngine = new AudioEngine(this);
            cameraEngine = new CameraVisionEngine(this);
            memoryEngine = new MemoryEngine(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            if (wakeLock != null) wakeLock.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        try {
            NotificationChannel channel = new NotificationChannel("smarteyex", "SmartEyeX Service", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, "smarteyex")
                .setContentTitle("SmartEyeX")
                .setContentText("AI Assistant Running")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
    }
}
