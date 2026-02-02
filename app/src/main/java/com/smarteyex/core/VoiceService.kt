package com.smarteyex.core

import android.app.Service
import android.content.Intent
import android.os.IBinder

class VoiceService : Service() {

    private lateinit var engine: VoiceEngine

    override fun onCreate() {
        super.onCreate()
        engine = VoiceEngine(this)
        engine.init()
        engine.startListening()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        engine.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}