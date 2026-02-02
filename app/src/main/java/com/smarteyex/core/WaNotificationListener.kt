package com.smarteyex.core

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class WaNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val bubbleMap = ConcurrentHashMap<String, MutableList<String>>()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!sbn.packageName.contains("whatsapp", true)) return

        val extras = sbn.notification.extras
        val sender = extras.getString("android.title") ?: return
        val text = extras.getCharSequence("android.text")?.toString() ?: return

        bubbleMap.getOrPut(sender) { mutableListOf() }.add(text)

        scope.launch {
            DelayManager.bubbleMergeDelay()
            val messages = bubbleMap.remove(sender) ?: return@launch
            val merged = messages.joinToString(", ")

            ConversationQueue.enqueue(sender, merged)
        }
    }
}