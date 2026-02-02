package com.smarteyex.core

import kotlinx.coroutines.*
import java.util.*

object ConversationQueue {

    private val queue: Queue<Pair<String, String>> = LinkedList()
    private val scope = CoroutineScope(Dispatchers.Default)

    fun enqueue(sender: String, message: String) {
        queue.add(sender to message)
        processQueue()
    }

    private fun processQueue() {
        scope.launch {
            if (NavigationStateManager.isBusy()) return@launch
            if (queue.isEmpty()) return@launch

            val (sender, message) = queue.poll()

            NavigationStateManager.setState(
                NavigationStateManager.State.SPEAKING
            )

            AppSpeak.say(
                "Pesan dari $sender, $message. Mau dibales apa diemin?"
            )

            NavigationStateManager.setState(
                NavigationStateManager.State.WAITING_USER_RESPONSE
            )
        }
    }
}