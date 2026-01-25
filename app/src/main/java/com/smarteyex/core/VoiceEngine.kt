package com.smarteyex.core

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.*
import com.smarteyex.core.voice.SpeechCommandProcessor

class VoiceEngine(private val context: Context) {

    private val speechRecognizer: SpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(context)

    private lateinit var tts: TextToSpeech
    private lateinit var processor: SpeechCommandProcessor

    private var listening = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("id", "ID")
            }
        }

        processor = SpeechCommandProcessor(context, tts)
    }

    fun toggleListening() {
        if (listening) stopListening()
        else startListening()
    }

    fun startListening() {
        listening = true

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
        listening = false
        speechRecognizer.stopListening()
    }

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}