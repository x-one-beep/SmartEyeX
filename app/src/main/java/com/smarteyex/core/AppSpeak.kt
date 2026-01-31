package com.smarteyex.core

import android.content.Context

object AppSpeak {
    lateinit var voice: VoiceEngine

    fun init(context: Context) {
        voice = VoiceEngine(context)
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if(onDone != null){
            voice.speak(text, onDone)
        } else {
            voice.speak(text) { /* kosong */ }
        }
    }
}