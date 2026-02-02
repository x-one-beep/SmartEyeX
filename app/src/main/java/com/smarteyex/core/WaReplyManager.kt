package com.smarteyex.core

object WaReplyManager {

    fun sendDirect(text: String) {
        NavigationStateManager.setState(
            NavigationStateManager.State.PROCESSING_REPLY
        )

        WaSender.send(text)

        NavigationStateManager.setState(
            NavigationStateManager.State.IDLE
        )

        ConversationQueue.enqueue("", "") // trigger lanjut queue
    }
}