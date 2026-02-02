package com.smarteyex.core.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

object VoiceService : TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var initialized = false
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            initialized = true
        }
    }

    fun speak(text: String) {
        if (!initialized) return
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, "SmartEyeX")
    }

    fun setSchoolMode(enabled: Boolean) {
        // adjust volume / speech rate
        tts.setSpeechRate(if (enabled) 0.7f else 1.0f)
    }

    fun setGameMode(enabled: Boolean) {
        tts.setSpeechRate(if (enabled) 1.2f else 1.0f)
    }

    fun setAlwaysListening(enabled: Boolean) {
        WakeWordEngine.setListening(enabled)
    }

    fun setEmotionLevel(level: Int) {
        // adjust pitch / tone based on AI emotion
        tts.setPitch(1.0f + (level / 10f))
    }

    fun setRestingMode(resting: Boolean) {
        if (resting) {
            tts.setSpeechRate(0.5f)
        } else {
            tts.setSpeechRate(1.0f)
        }
    }
}