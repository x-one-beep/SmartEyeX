package com.smarteyex.core

import kotlinx.coroutines.*
import java.util.*

object SafeNotifier {

    private val queue: Queue<String> = LinkedList()
    private val scope = CoroutineScope(Dispatchers.Default)

    fun enqueue(msg: String) {
        queue.add(msg)
        scope.launch {
            while (queue.isNotEmpty()) {
                delay(800)
                if (!NavigationStateManager.isBusy()
                    && AppState.aiMode == AppState.AiMode.ACTIVE
                ) {
                    AppSpeak.say(queue.poll())
                }
            }
        }
    }
}