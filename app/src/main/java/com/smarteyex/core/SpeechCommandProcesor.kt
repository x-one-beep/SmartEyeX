package com.smarteyex.core

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.*

class SpeechCommandProcessor(private val context: Context) {

    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private var callback: ((String) -> Unit)? = null

    init {
        speechRecognizer.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onResults(results: android.os.Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { command ->
                    processCommand(command)
                }
            }
            // Implementasi method lainnya (onError, dll.) untuk lengkap
            override fun onReadyForSpeech(params: android.os.Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onPartialResults(partialResults: android.os.Bundle?) {}
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })
    }

    // Fungsi untuk start listening perintah suara
    fun startListening(callback: (String) -> Unit) {
        this.callback = callback
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        speechRecognizer.startListening(intent)
    }

    // Fungsi untuk process perintah (e.g., "chat with AI", "reply WA")
    private fun processCommand(command: String) {
        when {
            command.contains("chat") -> GroqAiEngine().generateRandomResponse { response ->
                VoiceEngine().speak(response)
            }
            command.contains("reply") -> WaReplyManager().autoReply("Auto reply from voice")
            else -> callback?.invoke("Command not recognized")
        }
    }

    // Fungsi untuk stop listening
    fun stopListening() {
        speechRecognizer.stopListening()
    }
}