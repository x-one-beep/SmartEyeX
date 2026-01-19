package com.smarteyex.app.wa

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.app.PendingIntent
import android.app.RemoteInput

object WaReplyHelper {

    fun sendReply(
        context: Context,
        action: Notification.Action,
        replyText: String
    ) {
        val intent = Intent()
        val bundle = Bundle()

        for (input in action.remoteInputs) {
            bundle.putCharSequence(input.resultKey, replyText)
        }

        RemoteInput.addResultsToIntent(action.remoteInputs, intent, bundle)

        try {
            action.actionIntent.send(
                context,
                0,
                intent
            )
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
    }
}
