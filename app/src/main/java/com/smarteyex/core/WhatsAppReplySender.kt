package com.smarteyex.notification

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle

object WhatsAppReplySender {

    fun sendReply(sbn: android.service.notification.StatusBarNotification, reply: String) {

        val notification = sbn.notification
        val actions = notification.actions ?: return

        for (action in actions) {
            if (action.remoteInputs != null) {

                val intent = Intent()
                val bundle = Bundle()

                for (input in action.remoteInputs) {
                    bundle.putCharSequence(input.resultKey, reply)
                }

                RemoteInput.addResultsToIntent(action.remoteInputs, intent, bundle)
                action.actionIntent.send(null, 0, intent)
            }
        }
    }
}