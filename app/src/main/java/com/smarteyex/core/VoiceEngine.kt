package com.smarteyex.core.voice

import android.content.Context
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import com.smarteyex.core.AppState

object VoiceEngine {

    private lateinit var recognizer: SpeechRecognizer
    private lateinit var context: Context

    fun start(ctx: Context) {
        context = ctx
        recognizer = SpeechRecognizer.createSpeechRecognizer(ctx)

        recognizer.setRecognitionListener(VoiceListener)
        listen()
    }

    private fun listen() {
        val intent = RecognizerIntent().apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        recognizer.startListening(intent)
        AppState.isListening.set(true)
    }
if (AppState.awaitingWaReply) {
    WaReplyEngine.reply(
        AppState.lastWaNotification!!,
        spokenText
    )
    SpeechOutput.speak("udah gue kirim ya.")
}

    fun stop() {
        recognizer.stopListening()
        AppState.isListening.set(false)
    }
}