package com.example.smarteyex;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import androidx.core.app.NotificationCompat;

public class SmartEyeXService extends Service {
    private TextToSpeech tts;

    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(this, status -> {});
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, "SmartEyeX")
                .setContentTitle("SmartEyeX Active")
                .setContentText("AI Assistant is monitoring")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
        startForeground(1, notification);

        new AudioEngine(this).startAlwaysOn();
        new CameraVisionEngine(this).prepare();
        new SystemEventListener(this).startListening();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("SmartEyeX", "SmartEyeX Service", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }
}
