package com.smarteyex.core.wa

import android.app.PendingIntent
import android.app.RemoteInput
import android.os.Bundle
import android.service.notification.StatusBarNotification

class WaReplyManager {

    fun sendUserReply(
        sbn: StatusBarNotification,
        replyText: String
    ) {
        val actions = sbn.notification.actions ?: return

        for (action in actions) {
            val inputs = action.remoteInputs ?: continue

            val bundle = Bundle()
            for (input in inputs) {
                bundle.putCharSequence(input.resultKey, replyText)
            }

            val intent = android.content.Intent()
            RemoteInput.addResultsToIntent(inputs, intent, bundle)

            try {
                action.actionIntent.send(null, 0, intent)
            } catch (_: PendingIntent.CanceledException) {}
        }
    }
}