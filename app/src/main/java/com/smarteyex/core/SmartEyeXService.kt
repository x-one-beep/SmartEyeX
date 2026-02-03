package com.smarteyex.core

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.smarteyex.core.memory.MemoryItem
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

        startAlwaysListening()
        observeAppContext()
        startNotificationListener()
        speechCommand.startListening()
    }

    override fun onBind(intent: Intent): IBinder? { super.onBind(intent); return null }

    private fun startAlwaysListening() {
        lifecycleScope.launch {
            voiceEngine.startListening { speech ->
                if (!appState.isMicAllowed()) return@startListening
                if (appState.isConversationCrowded) return@startListening
                appState.updateFromSpeech(speech)
                memoryManager.addMemory(MemoryItem(System.currentTimeMillis().toString(), speech, 3))
                if (!shouldAiReact(speech)) return@startListening
                handleLiveConversation(speech)
            }
        }
    }

    private fun observeAppContext() {
        lifecycleScope.launch {
            appState.stateFlow.collect { state ->
                voiceEngine.adjustSensitivity(state.emotion, state.mode, appState.isBatteryLow)
            }
        }
    }

    private suspend fun handleLiveConversation(speech: String) {
        delay((400L..800L).random())
        val response = aiEngine.generateLiveResponse(speech, appState.currentEmotion, appState.currentContext)
        if (!response.shouldSpeak) return
        voiceService.speak(response.text, appState.currentEmotion)
    }

    private fun shouldAiReact(speech: String) =
        !appState.currentEmotion.isOverwhelmed() && !appState.isUserBusy && speech.containsTriggerForAi()

    private fun startNotificationListener() {
        notificationListener.setOnNewNotification { notif ->
            lifecycleScope.launch {
                if (appState.isUserBusy || appState.currentEmotion.isOverwhelmed()) return@launch
                voiceService.speak("Lo dapet notifikasi baru, mau gue bantu balas?", appState.currentEmotion)
            }
        }
    }
}