package com.smarteyex.core

import android.app.Application
import com.smarteyex.core.ai.GroqAiEngine
import com.smarteyex.core.memory.MemoryManager
import com.smarteyex.core.state.AppState
import com.smarteyex.core.voice.VoiceEngine
import com.smarteyex.core.voice.VoiceService

class SmartEyeXApp : Application() {

    lateinit var appState: AppState
    lateinit var voiceEngine: VoiceEngine
    lateinit var voiceService: VoiceService
    lateinit var aiEngine: GroqAiEngine
    lateinit var memoryManager: MemoryManager

    override fun onCreate() {
        super.onCreate()

        // Core state
        appState = AppState()

        // Voice system
        voiceEngine = VoiceEngine(appState)
        voiceService = VoiceService(this, appState)

        // Memory
        memoryManager = MemoryManager()

        // AI engine
        aiEngine = GroqAiEngine(appState, memoryManager)
    }
}
