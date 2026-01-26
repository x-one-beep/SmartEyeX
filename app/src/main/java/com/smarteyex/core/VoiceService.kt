package com.smarteyex.core

import android.app.*
import android.content.Intent
import android.os.*
import android.speech.*
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import com.smarteyex.app.R
import com.smarteyex.core.ai.GroqAiEngine
import java.util.*

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
        recognizer.setRecognitionListener(listener)

        startListening()
    }

    private val listener = object : RecognitionListener {

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
                    text,
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
            restart()
        }

        override fun onError(error: Int) { restart() }
        override fun onReadyForSpeech(p0: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(p0: Float) {}
        override fun onBufferReceived(p0: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(p0: Bundle?) {}
        override fun onEvent(p0: Int, p1: Bundle?) {}
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        recognizer.startListening(intent)
    }

    private fun restart() {
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

    private fun buildNotification(): Notification {
        val channelId = "SMART_EYE_X"
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                channelId,
                "SmartEyeX Voice",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("SmartEyeX aktif")
            .setContentText("Mendengarkan wake word")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }
}