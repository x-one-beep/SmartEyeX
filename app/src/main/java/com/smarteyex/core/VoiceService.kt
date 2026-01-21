package com.smarteyex.core

import android.app.Service
import android.content.Intent
import android.os.IBinder

class VoiceService : Service() {

    private lateinit var voice: VoiceEngine

    override fun onCreate() {
        super.onCreate()
        voice = VoiceEngine(this)
        voice.startListening {
            VoiceRouter.route(this, it)
        }
    }

    override fun onDestroy() {
        voice.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
