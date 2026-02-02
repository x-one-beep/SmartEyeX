package com.smarteyex.core.service

import android.content.Context
import com.smarteyex.core.VoiceService

object ServiceController {

    private var voiceService: VoiceService? = null

    fun startAll(context: Context) {
        if (voiceService == null) {
            voiceService = VoiceService(context)
            voiceService?.start()
        }
    }

    fun stopAll() {
        voiceService?.stop()
        voiceService = null
    }
}