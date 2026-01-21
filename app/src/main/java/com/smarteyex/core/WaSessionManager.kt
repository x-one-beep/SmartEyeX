package com.smarteyex.core

import android.app.Notification
import android.content.Context
import android.service.notification.StatusBarNotification

object WaSessionManager {

    fun onNewMessage(
        context: Context,
        sbn: StatusBarNotification,
        sender: String,
        text: String
    ) {
        val actions = sbn.notification.actions ?: return

        for (action in actions) {
            if (action.remoteInputs != null) {
                WaReplyHelper.sendReply(
                    context,
                    action,
                    "SmartEyeX aktif. Pesan diterima."
                )
                break
            }
        }
    }
}
