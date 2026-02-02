package com.smarteyex.core

import kotlinx.coroutines.delay
import kotlin.random.Random

object DelayManager {

    suspend fun shortHumanDelay() {
        delay(Random.nextLong(600, 1200))
    }

    suspend fun bubbleMergeDelay() {
        delay(Random.nextLong(1200, 2000))
    }

    suspend fun thinkingDelay() {
        delay(Random.nextLong(800, 1600))
    }
}