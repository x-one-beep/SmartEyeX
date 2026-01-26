package com.smarteyex.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.smarteyex.app.R

class VoiceService : Service() {

    private lateinit var recognizer: SpeechRecognizer
    private lateinit var tts: TextToSpeech
    private lateinit var ai: GroqAiEngine

    private var waitingWakeWord = true

    override fun onCreate() {
        super.onCreate()
        startForeground(99, buildNotification())

        ai = GroqAiEngine(this)

        tts = TextToSpeech(this) {
            tts.language = Locale("id", "ID")
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        startListening()
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.lowercase()
                    ?: return

                if (waitingWakeWord) {
                    if (text.contains("bung smart aktif")) {
                        waitingWakeWord = false
                        speak("Bung Smart aktif")
                    }
                } else {
                    ai.ask(
                        userText = text,
                        onResult = {
                            speak(it)
                            waitingWakeWord = true
                        },
                        onError = {
                            speak("AI bermasalah")
                            waitingWakeWord = true
                        }
                    )
                }

                restartListening()
            }

            override fun onError(error: Int) {
                restartListening()
            }

            override fun onReadyForSpeech(p0: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(p0: Bundle?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })

        recognizer.startListening(intent)
    }

    private fun restartListening() {
        recognizer.stopListening()
        recognizer.cancel()
        startListening()
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SMART_EYE_X")
    }

    override fun onDestroy() {
        recognizer.destroy()
        tts.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null
}