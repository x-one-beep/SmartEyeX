package com.smarteyex.core

import android.content.Context

object CoreController {

    private var running = false
    private lateinit var context: Context

    fun start(ctx: Context) {
        if (running) return
        running = true
        context = ctx.applicationContext

        // nanti: VoiceEngine.start()
        // nanti: NotificationWatcher.start()
        // nanti: EmotionEngine.start()
    }

    fun stop() {
        if (!running) return
        running = false

        // stop semua subsystem
    }
}