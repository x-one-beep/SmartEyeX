package com.smarteyex.voice

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.smarteyex.core.AppState
import com.smarteyex.notification.WhatsAppReplySender
import com.smarteyex.memory.SmartMemoryEngine
import com.smarteyex.core.voice.SpeechOutput

/* =========================
ENUM EMOSI SUARA
========================= */
enum class VoiceEmotion {
    CALM, HAPPY, SAD, TIRED, ANGRY, CARING, SERIOUS
}

/* =========================
PROSODY ENGINE
========================= */
class VoiceProsodyEngine {

    fun build(emotion: VoiceEmotion, intent: SpeechIntent): ProsodyProfile {
        return when (emotion) {
            VoiceEmotion.SAD -> ProsodyProfile(0.85f, 0.8f, 600, 1.0f)
            VoiceEmotion.HAPPY -> ProsodyProfile(1.1f, 1.05f, 250, 0.8f)
            VoiceEmotion.TIRED -> ProsodyProfile(0.9f, 0.75f, 700, 0.9f)
            VoiceEmotion.ANGRY -> ProsodyProfile(1.0f, 1.15f, 200, 0.2f)
            else -> ProsodyProfile(1.0f, 1.0f, 350, 0.6f)
        }
    }
}

data class ProsodyProfile(
    val pitch: Float,
    val speed: Float,
    val pauseMs: Int,
    val warmth: Float
)

enum class SpeechIntent {
    RESPOND, INFORM, INSTRUCT
}

/* =========================
VOICE REPLY CONTROLLER
========================= */
class VoiceReplyController {

    fun onUserVoiceAnswer(answer: String) {
        val notif = AppState.lastWaNotification ?: return

        SmartVoiceEngine.speak(
            "Oke, gue kirimin ya",
            emotion = VoiceEmotion.HAPPY,
            intent = SpeechIntent.RESPOND
        )

        WhatsAppReplySender.sendReply(notif, answer)
    }
}

/* =========================
VOICE INPUT CONTROLLER
========================= */
class VoiceInputController(private val context: Context) {

    fun startListening(onResult: (String) -> Unit) {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

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
                val text = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: return

                onResult(text)
            }
        })

        val intent = RecognizerIntent().apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        recognizer.startListening(intent)
    }
}

/* =========================
VOICE ENGINE UTAMA
========================= */
object SmartVoiceEngine {

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
                val spokenText = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: return

                AppState.lastSpokenText = spokenText

                // WA reply otomatis
                if (AppState.awaitingWaReply && AppState.lastWaNotification != null) {
                    WhatsAppReplySender.sendReply(AppState.lastWaNotification!!, spokenText)
                    SpeechOutput.speak("Udah gue kirim ya.")
                    AppState.awaitingWaReply = false
                }

                // Analisis konteks sosial & emosi user
                val emotion = runCatching {
                    SmartMemoryEngine(context).getEmotionalContext()
                }.getOrNull()

                // Output suara AI dengan emosi & prosody
                speak(spokenText, emotion = VoiceEmotion.CALM, intent = SpeechIntent.INFORM)
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

    fun speak(text: String, emotion: VoiceEmotion, intent: SpeechIntent) {
        val profile = VoiceProsodyEngine().build(emotion, intent)
        SpeechOutput.speak("$text") // Bisa dikombinasi pitch/speed/pause jika TTS mendukung
    }
}