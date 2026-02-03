package com.smarteyex.core

import android.app.Application
import com.smarteyex.core.ai.GroqAiEngine
import com.smarteyex.core.state.AppState
import com.smarteyex.core.voice.VoiceEngine
import com.smarteyex.core.voice.VoiceService

class SmartEyeXApp : Application() {
    lateinit var appState: AppState
    lateinit var voiceEngine: VoiceEngine
    lateinit var voiceService: VoiceService
    lateinit var aiEngine: GroqAiEngine
    lateinit var memoryManager: MemoryManager
    lateinit var notificationListener: NotificationListener
    lateinit var speechCommand: SpeechCommand
    lateinit var motionAnalyzer: MotionAnalyzer

    override fun onCreate() {
        super.onCreate()
        appState = AppState()
        memoryManager = MemoryManager()
        voiceEngine = VoiceEngine(appState)
        voiceService = VoiceService(this, appState)
        aiEngine = GroqAiEngine(appState, memoryManager)
        notificationListener = NotificationListener(this)
        speechCommand = SpeechCommand(this, appState)
        motionAnalyzer = MotionAnalyzer(this)
    }
}