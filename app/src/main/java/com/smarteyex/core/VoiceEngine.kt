package com.smarteyex.core

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.tts.UtteranceProgressListener

class VoiceService : Service() {

    private lateinit var voice: VoiceEngine
    private lateinit var ai: GroqAiEngine

    override fun onCreate() {
        super.onCreate()

        voice = VoiceEngine(this)
        ai = GroqAiEngine(this)

        voice.init {
            voice.speak("SmartEyeX aktif. Bung Smart siap.")
        }

        voice.getTts()?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {}
            override fun onError(utteranceId: String?) {}
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (it.action) {

                "AI_ASK" -> {
                    val question = it.getStringExtra("text") ?: return START_STICKY
                    ai.ask(question) { answer ->
                        voice.speak(answer)
                    }
                }

                "WA_MESSAGE" -> {
                    val sender = it.getStringExtra("sender") ?: "Seseorang"
                    val msg = it.getStringExtra("message") ?: ""
                    voice.speak("Pesan WhatsApp dari $sender. Isinya $msg")
                    ai.ask(msg) { answer ->
                        voice.speak(answer)
                    }
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        voice.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}