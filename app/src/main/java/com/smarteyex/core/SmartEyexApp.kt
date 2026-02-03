package com.smarteyex.core

import android.app.Application
import com.smarteyex.core.state.AppState
import com.smarteyex.core.voice.VoiceEngine
import com.smarteyex.core.voice.VoiceService
import com.smarteyex.core.ai.GroqAiEngine
import com.smarteyex.core.memory.MemoryManager

class SmartEyeXApp : Application() {

    // ===============================
    // CORE STATE
    // ===============================
    lateinit var appState: AppState
        private set

    // ===============================
    // VOICE
    // ===============================
    lateinit var voiceEngine: VoiceEngine
        private set
    lateinit var voiceService: VoiceService
        private set

    // ===============================
    // AI
    // ===============================
    lateinit var aiEngine: GroqAiEngine
        private set

    // ===============================
    // MEMORY
    // ===============================
    lateinit var memoryManager: MemoryManager
        private set

    // ===============================
    // NOTIF & COMMAND
    // ===============================
    lateinit var notificationListener: NotificationListener
        private set
    lateinit var speechCommand: SpeechCommand
        private set
    lateinit var motionAnalyzer: MotionAnalyzer
        private set

    override fun onCreate() {
        super.onCreate()

        // -------------------------------
        // 1️⃣ INIT APP STATE
        // -------------------------------
        appState = AppState()

        // -------------------------------
        // 2️⃣ INIT VOICE
        // -------------------------------
        voiceEngine = VoiceEngine(appState)
        voiceService = VoiceService(appState)

        // -------------------------------
        // 3️⃣ INIT MEMORY
        // -------------------------------
        memoryManager = MemoryManager(appState)

        // -------------------------------
        // 4️⃣ INIT AI ENGINE
        // -------------------------------
        aiEngine = GroqAiEngine(appState, memoryManager)

        // -------------------------------
        // 5️⃣ INIT NOTIF & COMMAND
        // -------------------------------
        notificationListener = NotificationListener(appState)
        speechCommand = SpeechCommand(appState)
        motionAnalyzer = MotionAnalyzer(appState)

        // -------------------------------
        // 6️⃣ START BACKGROUND SERVICE
        // -------------------------------
        SmartEyeXService.startService(this)
    }
}