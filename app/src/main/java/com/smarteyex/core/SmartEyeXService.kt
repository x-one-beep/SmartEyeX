package com.smarteyex.core

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.smarteyex.core.state.AppState
import com.smarteyex.core.voice.VoiceEngine
import com.smarteyex.core.voice.VoiceService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SmartEyeXService : LifecycleService() {

    private lateinit var appState: AppState
    private lateinit var voiceEngine: VoiceEngine
    private lateinit var voiceService: VoiceService
    private lateinit var aiEngine: GroqAiEngine
    private lateinit var memoryManager: MemoryManager
    private lateinit var notificationListener: NotificationListener
    private lateinit var speechCommand: SpeechCommand
    private lateinit var motionAnalyzer: MotionAnalyzer

    override fun onCreate() {
        super.onCreate()

        val app = application as SmartEyeXApp
        appState = app.appState
        voiceEngine = app.voiceEngine
        voiceService = app.voiceService
        aiEngine = app.aiEngine
        memoryManager = app.memoryManager
        notificationListener = app.notificationListener
        speechCommand = app.speechCommand
        motionAnalyzer = app.motionAnalyzer

        // Mulai semua subsistem
        startAlwaysListening()
        observeAppContext()
        startNotificationListener()
        speechCommand.startListening()
        motionAnalyzer.startAnalysis()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    /** ===============================
     * ALWAYS LISTENING & AI REACTION
     * ===============================
     */
    private fun startAlwaysListening() {
        lifecycleScope.launch {
            voiceEngine.startListening { speech ->
                if (!appState.isMicAllowed()) return@startListening
                if (appState.isConversationCrowded) return@startListening

                // Update emosi & context
                appState.updateFromSpeech(speech)
                memoryManager.addMemory(
                    MemoryItem(
                        id = System.currentTimeMillis().toString(),
                        text = speech,
                        importance = 3
                    )
                )

                if (!shouldAiReact(speech)) return@startListening
                handleLiveConversation(speech)
            }
        }
    }

    /** ===============================
     * OBSERVE APP CONTEXT & ADAPTIF
     * ===============================
     */
    private fun observeAppContext() {
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

    /** ===============================
     * LIVE CONVERSATION
     * ===============================
     */
    private suspend fun handleLiveConversation(speech: String) {
        delay((400L..800L).random()) // jeda alami manusia

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

    /** ===============================
     * AI SELF-RESTRAINT LOGIC
     * ===============================
     */
    private fun shouldAiReact(speech: String): Boolean {
        if (appState.currentEmotion.isOverwhelmed()) return false
        if (appState.isUserBusy) return false
        if (!speech.containsTriggerForAi()) return false

        return true
    }

    /** ===============================
     * NOTIFICATION LISTENER
     * ===============================
     */
    private fun startNotificationListener() {
        notificationListener.setOnNewNotification { notif ->
            lifecycleScope.launch {
                // cek prioritas & konteks user
                if (appState.isUserBusy || appState.currentEmotion.isOverwhelmed()) return@launch

                // AI tanya user dulu
                voiceService.speak(
                    "Lo dapet notifikasi baru, mau gue bantu balas?",
                    appState.currentEmotion
                )

                // Placeholder: integrasi WAReplyManager / Groq untuk balasan
                // notificationListener.autoReplyIfApproved(notif)
            }
        }
    }
}