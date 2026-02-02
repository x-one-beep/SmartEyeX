package com.smarteyex.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.*

class VoiceEngine(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isListening = false

    fun init() {
        initTTS()
        initSTT()
    }

    // ================= TTS =================
    private fun initTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("id", "ID")
                tts?.setSpeechRate(1.0f)
                tts?.setPitch(1.0f)
            }
        }
    }

    fun speak(text: String) {
        if (AppState.isSchoolSilent()) return
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "SmartEyeX")
    }

    fun stopSpeak() {
        tts?.stop()
    }

    // ================= STT =================
    private fun initSTT() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(listener)
    }

    fun startListening() {
        if (isListening || AppState.isGameMicOff()) return

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        speechRecognizer?.startListening(intent)
        isListening = true
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {
            AppState.markUserSpeaking(true)
        }

        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            AppState.markUserSpeaking(false)
        }

        override fun onError(error: Int) {
            isListening = false
            // auto-restart ringan
            AppState.schedule { startListening() }
        }

        override fun onResults(results: Bundle?) {
            isListening = false
            val texts =
                results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                ) ?: return

            val best = texts.firstOrNull() ?: return
            SpeechCommandProcessor.process(best)

            AppState.schedule { startListening() }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            // wake-word ringan tanpa keyword kaku
            val parts =
                partialResults?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                ) ?: return

            val p = parts.firstOrNull()?.lowercase() ?: return
            if (p.contains("smart") || p.contains("bung")) {
                AppState.wakeUp()
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun release() {
        speechRecognizer?.destroy()
        tts?.shutdown()
    }
}