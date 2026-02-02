package com.smarteyex.core

import android.app.Application
import com.smarteyex.core.state.AppState

class SmartEyeXApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // INIT GLOBAL STATE
        AppState.setMode(AppState.AppMode.NORMAL)
        AppState.setEmotion(AppState.Emotion.CALM)
        AppState.setListening(true)
        AppState.setAwake(true)

        // nanti:
        // init voice
        // init service
        // init groq
    }
}