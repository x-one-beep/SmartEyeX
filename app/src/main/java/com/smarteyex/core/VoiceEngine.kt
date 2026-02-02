package com.smarteyex.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class VoiceEngine(
    private val context: Context,
    private val onResult: (String) -> Unit
) {

    private var recognizer: SpeechRecognizer? = null
    private val listening = AtomicBoolean(false)

    private val wakeWords = listOf(
        "bung smart",
        "woi smart",
        "smart",
        "eh smart",
        "oi smart"
    )

    fun start() {
        if (listening.get()) return

        recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer?.setRecognitionListener(listener)
        listening.set(true)
        listen()
    }

    fun stop() {
        listening.set(false)
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }

    private fun listen() {
        if (!listening.get()) return

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        recognizer?.startListening(intent)
    }

    private val listener = object : RecognitionListener {

        override fun onResults(results: Bundle) {
            val texts =
                results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?: emptyList()

            handleTexts(texts)
            restart()
        }

        override fun onPartialResults(partialResults: Bundle) {
            val texts =
                partialResults.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                ) ?: return

            handleTexts(texts)
        }

        private fun handleTexts(texts: List<String>) {
            for (text in texts) {
                val lower = text.lowercase()

                // MODE GAME: cuma aktif kalo ada wake-word
                if (AppState.userMode == AppState.UserMode.GAME) {
                    if (wakeWords.any { lower.contains(it) }) {
                        AppState.aiMode = AppState.AiMode.ACTIVE
                        AppSpeak.say("Iya, apaan?")
                    }
                    continue
                }

                // MODE PASSIVE-AWARE
                if (AppState.aiMode == AppState.AiMode.PASSIVE_AWARE) {
                    if (wakeWords.any { lower.contains(it) }) {
                        AppState.aiMode = AppState.AiMode.ACTIVE
                        AppSpeak.say("Hmm?")
                        continue
                    }
                }

                if (AppState.aiMode == AppState.AiMode.ACTIVE) {
                    onResult(text)
                    break
                }
            }
        }

        private fun restart() {
            if (listening.get()) {
                listen()
            }
        }

        override fun onError(error: Int) {
            restart()
        }

        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onEndOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}