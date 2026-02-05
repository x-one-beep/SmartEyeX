package com.smarteyex.notification

import android.service.notification.StatusBarNotification

object PendingReplyCache {
    private var lastNotif: StatusBarNotification? = null

    fun store(sbn: StatusBarNotification) {
        lastNotif = sbn
    }

    fun get(): StatusBarNotification? = lastNotif
}