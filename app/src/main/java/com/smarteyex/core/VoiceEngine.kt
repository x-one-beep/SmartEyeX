package com.smarteyex.core

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class VoiceEngine(context: Context) : TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var isReady: Boolean = false
    private val callbackMap = mutableMapOf<String, () -> Unit>()

    init {
        tts = TextToSpeech(context, this)
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                utteranceId?.let {
                    callbackMap[it]?.invoke()
                    callbackMap.remove(it)
                }
            }
            override fun onError(utteranceId: String?) {}
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("id", "ID")
            isReady = true
        }
    }

    fun speak(text: String, onDone: () -> Unit) {
        if (!::tts.isInitialized || !isReady) return
        val utteranceId = System.currentTimeMillis().toString()
        callbackMap[utteranceId] = onDone
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}