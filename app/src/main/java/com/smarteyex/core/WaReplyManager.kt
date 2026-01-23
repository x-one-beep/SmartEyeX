package com.smarteyex.core.wa

import android.content.Context
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.app.PendingIntent
import android.os.Build
import android.os.Bundle

class WaAutoReplyManager(private val context: Context) {

    fun replyIfNeeded(
        sbn: StatusBarNotification,
        sender: String,
        message: String
    ) {
        // Filter basic (bisa dikembangin ke AI nanti)
        if (message.contains("halo", true) ||
            message.contains("ping", true) ||
            message.contains("assalam", true)
        ) {
            sendQuickReply(sbn, "Halo, ini SmartEyeX. Pesan diterima.")
        }
    }

    private fun sendQuickReply(
        sbn: StatusBarNotification,
        replyText: String
    ) {
        val notification = sbn.notification
        val actions = notification.actions ?: return

        for (action in actions) {
            if (action.remoteInputs != null) {
                val intent = action.actionIntent
                val bundle = Bundle()

                for (input in action.remoteInputs) {
                    bundle.putCharSequence(input.resultKey, replyText)
                }

                val replyIntent = android.content.Intent()
                android.app.RemoteInput.addResultsToIntent(
                    action.remoteInputs,
                    replyIntent,
                    bundle
                )

                try {
                    intent.send(context, 0, replyIntent)
                } catch (e: PendingIntent.CanceledException) {
                    e.printStackTrace()
                }
            }
        }
    }
}