package com.smarteyex.core.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.smarteyex.core.SmartEyeXApp
import com.smarteyex.core.state.AppState
import com.smarteyex.core.voice.VoiceEngine
import com.smarteyex.core.voice.VoiceService
import com.smarteyex.core.ai.GroqAiEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SmartEyeXService : LifecycleService() {

    private lateinit var appState: AppState
    private lateinit var voiceEngine: VoiceEngine
    private lateinit var voiceService: VoiceService
    private lateinit var aiEngine: GroqAiEngine

    override fun onCreate() {
        super.onCreate()

        val app = application as SmartEyeXApp
        appState = app.appState
        voiceEngine = app.voiceEngine
        voiceService = app.voiceService
        aiEngine = app.aiEngine

        startAlwaysListening()
        observeContext()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    /**
     * ===============================
     * ALWAYS LISTENING (ADAPTIF)
     * ===============================
     */
    private fun startAlwaysListening() {
        lifecycleScope.launch {
            voiceEngine.startListening { speech ->

                if (!appState.isMicAllowed()) return@startListening
                if (appState.isConversationCrowded) return@startListening

                appState.updateFromSpeech(speech)

                if (!shouldAiReact(speech)) return@startListening

                handleLiveConversation(speech)
            }
        }
    }

    /**
     * ===============================
     * LIVE CONTEXT OBSERVER
     * ===============================
     * Waktu, emosi, mode, battery
     */
    private fun observeContext() {
        lifecycleScope.launch {
            appState.stateFlow.collect { state ->
                // adaptif, tidak cerewet
                voiceEngine.adjustSensitivity(
                    emotion = state.emotion,
                    mode = state.currentMode,
                    batteryLow = state.isBatteryLow
                )
            }
        }
    }

    /**
     * ===============================
     * AI LIVE RESPONSE
     * ===============================
     */
    private suspend fun handleLiveConversation(speech: String) {
        delay((400L..800L).random()) // jeda manusia

        val response = aiEngine.generateLiveResponse(
            speech = speech,
            emotion = appState.currentEmotion,
            context = appState.currentContext
        )

        if (!response.shouldSpeak) return

        voiceService.speak(
            text = response.text,
            emotion = appState.currentEmotion
        )
    }

    /**
     * ===============================
     * SELF RESTRAINT CORE
     * ===============================
     */
    private fun shouldAiReact(speech: String): Boolean {
        if (appState.currentEmotion.isOverwhelmed()) return false
        if (appState.isUserBusy) return false
        if (!speech.containsTriggerForAi()) return false

        return true
    }
}