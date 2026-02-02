package com.smarteyex.core

import android.content.Context
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import java.util.*

class VoiceEngine(
    private val context: Context,
    private val onResult: (String) -> Unit
) {

    private val speechRecognizer: SpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(context)

    private val tts: TextToSpeech =
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("id", "ID")
                tts.setSpeechRate(0.95f)
            }
        }

    init {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val texts =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!texts.isNullOrEmpty()) {
                    onResult(texts[0].lowercase(Locale.getDefault()))
                }
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {
                AppState.isListening = true
            }

            override fun onEndOfSpeech() {
                AppState.isListening = false
            }

            override fun onError(error: Int) {
                AppState.isListening = false
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun listen() {
        val intent = RecognizerIntent().apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
        }
        speechRecognizer.startListening(intent)
    }

    fun speak(text: String) {
        AppState.isSpeaking = true
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString())
        AppState.isSpeaking = false
    }

    fun shutdown() {
        speechRecognizer.destroy()
        tts.shutdown()
    }
}