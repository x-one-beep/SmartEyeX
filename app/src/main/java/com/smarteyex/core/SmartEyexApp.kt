package com.smarteyex.core

import android.app.Service
import android.content.Context
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

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, SmartEyeXService::class.java)
            context.startService(intent)
        }
    }

    private lateinit var appState: AppState
    private lateinit var voiceEngine: VoiceEngine
    private lateinit var voiceService: VoiceService
    private lateinit var aiEngine: GroqAiEngine
    private lateinit var memoryManager: MemoryManager
    private lateinit var notificationListener: NotificationListener
    private lateinit var speechCommand: SpeechCommand
    private lateinit var motionAnalyzer: MotionAnalyzer
    private lateinit var waReplyManager: WAReplyManager

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
        waReplyManager = WAReplyManager(appState, aiEngine)

        startVoiceListening()
        observeAppContext()
        startNotificationListener()
        speechCommand.startListening()
        motionAnalyzer.start()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    /** ===============================
     * ALWAYS LISTENING & AI REACTION
     * ===============================
     */
    private fun startVoiceListening() {
        lifecycleScope.launch {
            voiceEngine.startListening { speech ->
                if (!appState.isMicAllowed()) return@startListening
                if (appState.isConversationCrowded) return@startListening

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
        delay((400L..800L).random()) // jeda alami

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
        if (appState.isAIResting) return false
        return true
    }

    /** ===============================
     * NOTIFICATION LISTENER & WA REPLY
     * ===============================
     */
    private fun startNotificationListener() {
        notificationListener.setOnNewNotification { notif ->
            lifecycleScope.launch {
                // cek konteks user
                if (appState.isUserBusy || appState.currentEmotion.isOverwhelmed()) return@launch

                // AI tanya user dulu
                voiceService.speak(
                    "Lo dapet notifikasi baru, mau gue bantu balas?",
                    appState.currentEmotion
                )

                // WA reply manager handle jika user setuju
                waReplyManager.queueNotification(notif)
            }
        }
    }
}