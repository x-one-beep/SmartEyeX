package com.smarteyex.core

class MotionAnalyzer {

    private var lastState = "STILL"

    fun analyze(motionValue: Float): String {
        lastState = when {
            motionValue > 8f -> "MOVING FAST"
            motionValue > 2f -> "MOVING"
            else -> "STILL"
        }
        return lastState
    }

    fun getLastState(): String = lastState
}
