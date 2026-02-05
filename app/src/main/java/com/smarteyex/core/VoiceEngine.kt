package com.smarteyex.core.voice

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.smarteyex.core.AppState
import com.smarteyex.notification.WaReplyEngine
import com.smarteyex.memory.SmartMemoryEngine

object VoiceEngine {

    private lateinit var recognizer: SpeechRecognizer
    private lateinit var context: Context

    fun start(ctx: Context) {
        context = ctx
        recognizer = SpeechRecognizer.createSpeechRecognizer(ctx)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onResults(results: Bundle) {
                val spokenText = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: return

                AppState.lastSpokenText = spokenText

                // === WA reply otomatis jika AI menunggu balasan ===
                if (AppState.awaitingWaReply && AppState.lastWaNotification != null) {
                    WaReplyEngine.reply(AppState.lastWaNotification!!, spokenText)
                    SpeechOutput.speak("Udah gue kirim ya.")
                    AppState.awaitingWaReply = false
                }

                // === Analisis konteks sosial & emosi user ===
                val mode = socialEngine.evaluate(
                    SocialSignal(
                        speakerCount = AppState.currentSpeakerCount,
                        avgSpeechSpeed = AppState.currentSpeechSpeed,
                        emotionLevel = AppState.currentEmotionLevel,
                        keywordTrigger = AppState.keywordDetected,
                        userMentionedAI = AppState.userMentionedAI
                    )
                )

                // AI bisa menyesuaikan nada & cara respon
                val emotion = SmartMemoryEngine(context).getEmotionalContext()
                VoiceOutput.speak(spokenText, emotion = emotion, persona = mode.persona)
            }
        })

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

    fun stop() {
        recognizer.stopListening()
        AppState.isListening.set(false)
    }
}