package com.smarteyex.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import com.smarteyex.core.MainActivity
import com.smarteyex.core.memory.MemoryManager
import com.smarteyex.core.ai.GroqAiEngine


class SpeechCommandProcessor(
    private val context: Context,
    private val tts: TextToSpeech,
    private val ai: GroqAiEngine
) : RecognitionListener {

    override fun onResults(results: Bundle?) {
        val text = results
            ?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?: return

        if (text.contains("halo smart eye", true)) {
            tts.speak("Ya Bung X, saya dengar", TextToSpeech.QUEUE_FLUSH, null, null)
            return
        }

        ai.ask(
            userText = text,
            onResult = { answer ->
                tts.speak(answer, TextToSpeech.QUEUE_FLUSH, null, "AI")
            },
            onError = {
                tts.speak("Koneksi AI bermasalah", TextToSpeech.QUEUE_FLUSH, null, null)
            }
        )
    }

    override fun onError(error: Int) {}
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}