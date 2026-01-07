package com.example.smarteyex;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;

public class NotificationListener extends NotificationListenerService {
    private TextToSpeech tts;

    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(this, status -> {});
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String text = sbn.getNotification().extras.getString("android.text");
        if (text != null) {
            AICore ai = new AICore();
            String summary = ai.analyzeNotification(text);
            tts.speak("Bung, ada pesan: " + summary, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
}
