package com.smarteyex.core

import kotlinx.coroutines.*

object Async {

    private val scope = CoroutineScope(
        Dispatchers.IO + SupervisorJob()
    )

    fun run(block: suspend () -> Unit) {
        scope.launch {
            block()
        }
    }
}
