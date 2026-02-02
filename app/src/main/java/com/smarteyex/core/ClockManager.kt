package com.smarteyex.core

import kotlinx.coroutines.*

object ClockManager {

    private var job: Job? = null

    fun start() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val idleTime = System.currentTimeMillis() - AppState.lastActiveTimestamp
                if (idleTime > 5 * 60 * 1000) {
                    AppState.aiMode = AppState.AiMode.PASSIVE_AWARE
                }
                delay(30_000)
            }
        }
    }

    fun stop() {
        job?.cancel()
    }
}