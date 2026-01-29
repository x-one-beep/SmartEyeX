package com.smarteyex.core

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.speech.*
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import com.smarteyex.core.ai.GroqAiEngine


class VoiceService : Service() {

    private lateinit var recognizer: SpeechRecognizer
    private lateinit var voice: VoiceEngine
    private lateinit var aiEngine: GroqAiEngine
    private var isActive = false

    override fun onCreate() {
        super.onCreate()

        startForeground(1, buildNotification())

        voice = VoiceEngine(this)
        aiEngine = GroqAiEngine(this)
        
        // ✅ Pastikan mic permission dulu
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                recognizer = SpeechRecognizer.createSpeechRecognizer(this)
                recognizer.setRecognitionListener(listener)
                startListening()
            } catch (e: Exception) {
                voice.speak("Voice engine gagal dijalankan")
                stopSelf()
            }
        } else {
            voice.speak("Izin mikrofon belum diberikan")
            stopSelf()
        }
    }

    private val listener = object : RecognitionListener {

        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.lowercase()
                ?: return
if (!isActive) {

    if (text.contains("bung smart aktif")) {
        isActive = true
        voice.speak("Bung Smart aktif")
    }

    restartListening()
    return
}
if (text.isNotBlank()) {
    recognizer.cancel()
    askAI(text)
} else {
    restartListening()
}
}
            

        override fun onError(error: Int) {
            restartListening()

        }

        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
private fun askAI(text: String) {
if (!::recognizer.isInitialized) return
    
    aiEngine.ask(
        text,
        onResult = { answer ->
            voice.speak(answer)
            restartListening()
        },
        onError = {
            voice.speak("AI tidak merespon")
            restartListening()
        }
    )
}

    private fun startListening() {
        if (!::recognizer.isInitialized) return

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }
        recognizer.startListening(intent)
    }

    private fun restartListening() {
        if (::recognizer.isInitialized) {
            recognizer.cancel()
            startListening()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {

            "AI_ASK" -> {
                val text = intent.getStringExtra("text") ?: return START_STICKY
                aiEngine.ask(
                    text,
                    onResult = { answer ->
                        voice.speak(answer)
                    },
                    onError = {
                        voice.speak("Maaf Bung, sistem bermasalah")
                    }
                )
            }
}

            

        return START_STICKY
    }

    override fun onDestroy() {
        if (::recognizer.isInitialized) recognizer.destroy()
        voice.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun buildNotification(): Notification {
        val channelId = "SMART_EYE_X"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SmartEyeX Voice",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("SmartEyeX aktif")
            .setContentText("Mendengarkan Bung Smart")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }
}