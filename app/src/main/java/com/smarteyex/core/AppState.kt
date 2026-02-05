package com.smarteyex.core

import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean

object AppState {

    private lateinit var appContext: Context

    val isBusyMode = AtomicBoolean(false)
    val isSchoolMode = AtomicBoolean(false)
    val isListening = AtomicBoolean(false)

    var currentEmotion: Emotion = Emotion.NEUTRAL
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun setEmotion(emotion: Emotion) {
        currentEmotion = emotion
    }

    enum class Emotion {
        HAPPY,
        SAD,
        TIRED,
        ANGRY,
        EMPTY,
        NEUTRAL
    }
}