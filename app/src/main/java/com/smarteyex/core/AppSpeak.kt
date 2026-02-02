package com.smarteyex.core

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

object AppSpeak : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val ready = AtomicBoolean(false)
    private val speaking = AtomicBoolean(false)
    private val queue = ConcurrentLinkedQueue<String>()

    fun init(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("id", "ID")
            ready.set(true)
            flushQueue()
        }
    }

    /**
     * ENTRY UTAMA – semua suara lewat sini
     */
    fun say(text: String) {
        if (!ready.get()) {
            queue.add(text)
            return
        }
        queue.add(text)
        flushQueue()
    }

    /**
     * Jangan motong omongan
     */
    private fun flushQueue() {
        if (speaking.get()) return
        val next = queue.poll() ?: return
        speaking.set(true)

        applyEmotion()

        tts?.speak(
            next,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "SMARTEYEX_${System.currentTimeMillis()}"
        )

        tts?.setOnUtteranceProgressListener(
            SimpleUtteranceListener(
                onDone = {
                    speaking.set(false)
                    flushQueue()
                },
                onError = {
                    speaking.set(false)
                    flushQueue()
                }
            )
        )
    }

    /**
     * EMOSI NGARUH KE INTONASI
     */
    private fun applyEmotion() {
        when (AppState.emotion) {
            AppState.Emotion.SENENG -> {
                tts?.setPitch(1.15f)
                tts?.setSpeechRate(1.05f)
            }
            AppState.Emotion.SEDIH -> {
                tts?.setPitch(0.9f)
                tts?.setSpeechRate(0.9f)
            }
            AppState.Emotion.KESEL -> {
                tts?.setPitch(1.2f)
                tts?.setSpeechRate(1.1f)
            }
            else -> {
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(1.0f)
            }
        }
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        ready.set(false)
        speaking.set(false)
        queue.clear()
    }
}