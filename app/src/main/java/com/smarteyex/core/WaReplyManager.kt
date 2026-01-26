package com.smarteyex.core.wa

import android.app.PendingIntent
import android.os.Bundle
import android.service.notification.StatusBarNotification

class WaReplyManager {

    fun sendUserReply(
        sbn: StatusBarNotification,
        replyText: String
    ) {
        val actions = sbn.notification.actions ?: return

        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue

            val intent = action.actionIntent
            val bundle = Bundle()

            for (input in remoteInputs) {
                bundle.putCharSequence(input.resultKey, replyText)
            }

            val replyIntent = android.content.Intent()
            android.app.RemoteInput.addResultsToIntent(
                remoteInputs,
                replyIntent,
                bundle
            )

            try {
                intent.send(null, 0, replyIntent)
            } catch (e: PendingIntent.CanceledException) {
                e.printStackTrace()
            }
        }
    }
}