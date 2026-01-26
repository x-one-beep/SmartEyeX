package com.smarteyex.core

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import com.smarteyex.core.ai.GroqAiEngine
import java.util.*

class VoiceEngine(private val context: Context) {

    private val speechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(context)

    private lateinit var tts: TextToSpeech
    private lateinit var processor: SpeechCommandProcessor

    fun init(ai: GroqAiEngine) {
        tts = TextToSpeech(context) {
            tts.language = Locale("id", "ID")
        }
        processor = SpeechCommandProcessor(context, tts, ai)
        speechRecognizer.setRecognitionListener(processor)
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
        }
        speechRecognizer.startListening(intent)
    }

    fun stop() {
        speechRecognizer.stopListening()
        tts.shutdown()
    }
}