package com.smarteyex.core

import android.app.*
import android.content.Intent
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import com.smarteyex.core.wa.WaReplyManager
import android.service.notification.StatusBarNotification

class VoiceService : Service() {

    private lateinit var recognizer: SpeechRecognizer
    private lateinit var voice: VoiceEngine
    private lateinit var waReplyManager: WaReplyManager

    private enum class Mode { IDLE, ACTIVE, WA_REPLY }
    private var mode = Mode.IDLE
    private lateinit var lastWaNotification: StatusBarNotification

    override fun onCreate() {
        super.onCreate()

        startForeground(1, buildNotification())

        voice = VoiceEngine(this)
        voice.init()

        waReplyManager = WaReplyManager()

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

            when (mode) {
                Mode.IDLE -> {
                    if (text.contains("bung smart aktif")) {
                        voice.speak("Bung Smart aktif")
                        mode = Mode.ACTIVE
                    }
                }
                Mode.ACTIVE -> {
    aiEngine.ask(text,
        onResult = { answer ->
            voice.speak(answer) // ini baru suara jawaban Groq
        },
        onError = {
            voice.speak("Maaf, gagal merespon")
        }
    )
}

                Mode.WA_REPLY -> {
                    waReplyManager.sendUserReply(lastWaNotification, text)
                    voice.speak("Pesan terkirim")
                    mode = Mode.IDLE
                }
            }
            restartListening()
        }

        override fun onError(error: Int) { restartListening() }
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        recognizer.startListening(intent)
    }

    private fun restartListening() {
        recognizer.cancel()
        startListening()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "WA_MESSAGE") {
            lastWaNotification = intent.getParcelableExtra("sbn")!!
            val sender = intent.getStringExtra("sender")!!
            val message = intent.getStringExtra("message")!!

            mode = Mode.WA_REPLY
            voice.speak("Pesan WhatsApp dari $sender. Isinya $message. Silakan jawab.")
        }
        return START_STICKY
    }

    override fun onDestroy() {
        recognizer.destroy()
        voice.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun buildNotification(): Notification {
        val channelId = "SMART_EYE_X"
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "SmartEyeX Voice", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("SmartEyeX aktif")
            .setContentText("Mendengarkan Bung Smart")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }
}