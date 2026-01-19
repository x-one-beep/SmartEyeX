package com.smarteyex.core.wa

import android.content.Context
import android.service.notification.StatusBarNotification
import com.smarteyex.core.tts.TextToSpeechManager

object WaSessionManager {

    private var lastMessages = mutableMapOf<String, StatusBarNotification>()
    var soundEnabled = true

    fun onNewMessage(
        context: Context,
        sbn: StatusBarNotification,
        sender: String,
        message: String
    ) {
        lastMessages[sender.lowercase()] = sbn

        if (soundEnabled) {
            TextToSpeechManager(context)
                .speak("Ada chat dari $sender. $message")
        }
    }

    fun handleVoiceCommand(
        context: Context,
        spokenText: String
    ) {
        val command = VoiceCommandParser.parse(spokenText)

        when (command.type) {
            CommandType.REPLY -> {
                val sbn = lastMessages[command.target] ?: return
                WaReplyHelper.reply(context, sbn, command.message)
            }

            CommandType.READ -> {
                // cukup tandai read (notif dihapus)
                lastMessages[command.target]?.let {
                    context.sendBroadcast(it.notification.contentIntent)
                }
            }

            CommandType.IGNORE -> {
                // tidak lakukan apa-apa
            }
        }
    }
}
