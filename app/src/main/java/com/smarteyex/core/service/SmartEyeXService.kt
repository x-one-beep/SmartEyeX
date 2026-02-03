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
import com.smarteyex.core.memory.MemoryManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SmartEyeXService : LifecycleService() {

    private lateinit var appState: AppState
    private lateinit var voiceEngine: VoiceEngine
    private lateinit var voiceService: VoiceService
    private lateinit var aiEngine: GroqAiEngine
    private lateinit var memoryManager: MemoryManager

    override fun onCreate() {
        super.onCreate()

        val app = application as SmartEyeXApp
        appState = app.appState
        voiceEngine = app.voiceEngine
        voiceService = app.voiceService
        aiEngine = app.aiEngine
        memoryManager = app.memoryManager

        startAlwaysListening()
        observeContext()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    /**
     * ===============================
     * ALWAYS LISTENING & LIVE PROCESS
     * ===============================
     */
    private fun startAlwaysListening() {
        lifecycleScope.launch {
            voiceEngine.startListening { speech ->

                // Cek mic & crowd
                if (!appState.isMicAllowed()) return@startListening
                if (appState.isConversationCrowded) return@startListening

                // Update emosi & konteks
                appState.updateFromSpeech(speech)

                // Simpan memory
                memoryManager.addMemory(
                    com.smarteyex.core.memory.MemoryItem(
                        id = System.currentTimeMillis().toString(),
                        text = speech,
                        importance = 3
                    )
                )

                // Cek AI harus nimbrung?
                if (!shouldAiReact(speech)) return@startListening

                handleLiveConversation(speech)
            }
        }
    }

    /**
     * ===============================
     * OBSERVE APP CONTEXT
     * ===============================
     */
    private fun observeContext() {
        lifecycleScope.launch {
            appState.stateFlow.collect { state ->
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
     * HANDLE AI RESPONSE
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
     * SELF-RESTRAINT / NIMBRUNG LOGIC
     * ===============================
     */
    private fun shouldAiReact(speech: String): Boolean {
        if (appState.currentEmotion.isOverwhelmed()) return false
        if (appState.isUserBusy) return false
        if (!speech.containsTriggerForAi()) return false

        return true
    }
}