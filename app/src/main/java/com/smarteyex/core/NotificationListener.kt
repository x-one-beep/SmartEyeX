package com.smarteyex.core

class NotificationItem(
    val packageName: String,
    val title: String,
    val content: String
)

class NotificationListener {

    private var callback: ((NotificationItem) -> Unit)? = null

    fun setOnNewNotification(cb: (NotificationItem) -> Unit) {
        callback = cb
    }

    /**
     * Simulasi notif baru
     */
    fun simulateIncomingNotification(notif: NotificationItem) {
        callback?.invoke(notif)
    }

    /**
     * Hook untuk WA / sistem notif
     * Integrasi nanti dengan WAAccessibility / NotificationManager
     */
    fun onNotificationReceived(notif: NotificationItem) {
        callback?.invoke(notif)
    }
}