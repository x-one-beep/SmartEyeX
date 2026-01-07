package com.smarteyex.service;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.smarteyex.ai.AudioEngine;
import com.smarteyex.ai.MemoryEngine;

public class NotificationListener extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String text = sbn.getNotification().extras.getString("android.text");
            if (text != null) {
                MemoryEngine memory = new MemoryEngine(this);
                memory.saveLog("Notification: " + text);
                AudioEngine audio = new AudioEngine(this);
                audio.speak(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
