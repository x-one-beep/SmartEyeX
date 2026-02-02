package com.smarteyex.core

import kotlinx.coroutines.*
import java.time.*

object SmartReminder {

    private val scope = CoroutineScope(Dispatchers.Default)

    fun scheduleIfConfirmed(
        title: String,
        dateTime: LocalDateTime
    ) {
        val now = LocalDateTime.now()
        if (dateTime.isBefore(now)) return

        val delayMs = Duration.between(now, dateTime.minusHours(1)).toMillis()
        if (delayMs <= 0) return

        scope.launch {
            delay(delayMs)
            InterruptionPolicy.notifyWhenSafe(
                "Eh, bentar lagi $title. Jangan sampe telat."
            )
        }
    }
}

