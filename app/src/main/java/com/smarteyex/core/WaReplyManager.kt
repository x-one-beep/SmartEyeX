package com.smarteyex.core

object WaReplyManager {

    private var currentSender: String? = null

    fun prepareReply(sender: String) {
        currentSender = sender
        SpeechCommandProcessor.setWaitingWaReply(true)
    }

    fun sendDirect(text: String) {
        val sender = currentSender ?: return

        AppState.enqueueTask {
            WaSender.send(text)
        }

        currentSender = null
    }
}
