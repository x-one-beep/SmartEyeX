package com.smarteyex.core

import android.content.Context

object AppSpeak {
    lateinit var voice: VoiceEngine

    fun init(context: Context) {
        voice = VoiceEngine(context)
    }

    fun speak(text: String) {
        voice.speak(text)
    }
}