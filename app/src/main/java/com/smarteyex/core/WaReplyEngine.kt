package com.smarteyex.core.service

import android.app.RemoteInput
import android.service.notification.StatusBarNotification

object WaReplyEngine {

    fun reply(sbn: StatusBarNotification, text: String) {
        val action = sbn.notification.actions.firstOrNull {
            it.remoteInputs != null
        } ?: return

        val input = Bundle().apply {
            putCharSequence(action.remoteInputs[0].resultKey, text)
        }

        val intent = Intent()
        RemoteInput.addResultsToIntent(action.remoteInputs, intent, input)
        action.actionIntent.send(applicationContext, 0, intent)
    }
}