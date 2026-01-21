package com.smarteyex.core

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.app.RemoteInput

object WaReplyHelper {

    fun sendReply(
        context: Context,
        action: Notification.Action,
        text: String
    ) {
        val intent = Intent()
        val bundle = Bundle()
        for (input in action.remoteInputs) {
            bundle.putCharSequence(input.resultKey, text)
        }
        RemoteInput.addResultsToIntent(action.remoteInputs, intent, bundle)
        action.actionIntent.send(context, 0, intent)
    }
}
