package com.smarteyex.core.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.smarteyex.core.state.AppState
import com.smarteyex.core.voice.VoiceService

class SmartEyeXService : Service() {

    override fun onCreate() {
        super.onCreate()
        VoiceService.init(applicationContext)
        startBackgroundListeners()
    }

    private fun startBackgroundListeners() {
        // Simulasi WA listener & mic listener
        Thread {
            while (!AppState.isAIResting) {
                // cek notif WA
                // cek voice command
                Thread.sleep(1000)
            }
        }.start()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}