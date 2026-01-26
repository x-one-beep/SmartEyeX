package com.smarteyex.core

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.*

class VoiceEngine(private val context: Context) {

    private val speechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(context)

    private val tts = TextToSpeech(context) {
        if (it == TextToSpeech.SUCCESS) {
            tts.language = Locale("id", "ID")
        }
    }

    private val ai = GroqAiEngine(context)

    private val processor =
        SpeechCommandProcessor(context, tts, ai)

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
        }

        speechRecognizer.setRecognitionListener(processor)
        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
    }
}