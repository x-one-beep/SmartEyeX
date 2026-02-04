package com.smarteyex.core

import android.content.Context
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import android.speech.tts.TextToSpeech
import androidx.lifecycle.LifecycleService

object PublicSafetyLayer {
    fun monitorHealth() {}
    fun pauseAI(reason: String) {}
    fun resumeAI() {}
    fun isPaused() = false
}

object UserBoundaryManager {
    enum class UserState { BUSY, AVAILABLE }
    var currentState: UserState = UserState.AVAILABLE
    fun observe() {}
}

class PartnerIdamanEngine(
    private val appState: AppState,
    private val voiceService: VoiceService,
    private val memoryManager: MemoryEngine
) {
    fun maybePraiseUser() {}
    fun maybeRandomPraise() {}
}

/* =========================================================
   SmartEyeXIntegrator — dummy engine jadi nyata
   API key otomatis ambil dari environment (GitHub secret)
========================================================= */

class SmartEyeXIntegrator(
    private val context: Context,
    private val service: LifecycleService? = null // optional untuk attach watchdog
) {

    // Ambil API key dari environment / secret GitHub
    private val groqApiKey = System.getenv("SMART_EYE_X_API_KEY") ?: "DUMMY_API_KEY"

    // Core engines
    private val voiceEngine = VoiceEngine(context)
    private val aiEngine = GroqAiEngine(groqApiKey)
    private val memoryEngine = MemoryEngine
    private val voiceService = VoiceService(context)
    private val motionAnalyzer = MotionAnalyzer(context, memoryEngine)
    private val waReplyManager = WAReplyManager(AppState, voiceService, memoryEngine, aiEngine)
    private val waAccessibility = WAAccessibility(context, AppState, voiceService, memoryEngine)
    private val notificationListener = NotificationListener(AppState, voiceService, memoryEngine, aiEngine, waAccessibility, waReplyManager)
    private val speechCommand = SpeechCommand(AppState, voiceEngine, voiceService, aiEngine, memoryEngine)
    private val partnerEngine = PartnerIdamanEngine(AppState, voiceService, memoryEngine)

    init {
        setup()
    }

    private fun setup() {

        // -------------------------
        // Attach hooks supaya dummy jadi nyata
        // -------------------------
        voiceEngine.attachSmartEyeXHooks()
        motionAnalyzer.markMotionDetected()
        notificationListener.markNotificationArrived()

        // -------------------------
        // Start watchdog & background tasks
        // -------------------------
        service?.startSmartEyeXWatchdog(AppState)
        PublicSafetyLayer.monitorHealth()
        UserBoundaryManager.observe()

        // -------------------------
        // Start listening & commands
        // -------------------------
        startVoiceListening()
        startSpeechCommands()

        // -------------------------
        // Start motion & notifications
        // -------------------------
        startMotionMonitoring()
        startNotificationMonitoring()
    }

    private fun startVoiceListening() {
        voiceEngine.startListening { speech ->
            // update lastVoiceDetectedAt
            SmartEyeXRuntime.lastVoiceDetectedAt = System.currentTimeMillis()
            // masuk memory
            memoryEngine.addMemory(MemoryItem(System.currentTimeMillis().toString(), "User said: $speech", 2))
            // trigger AI langsung
            handleAIResponse(speech)
        }
    }

    private fun startSpeechCommands() {
        speechCommand.startListening()
    }

    private fun startMotionMonitoring() {
        motionAnalyzer.startMonitoring()
    }

    private fun startNotificationMonitoring() {
        // simulate polling notifikasi WA / sistem
        GlobalScope.launch {
            while (true) {
                delay(3000)
                val dummyNotif = WANotification(
                    text = "Hello, ini dummy notif!",
                    sender = "WhatsApp",
                    timestamp = System.currentTimeMillis()
                )
                notificationListener.handleNewNotification(dummyNotif)
            }
        }
    }

    private fun handleAIResponse(speech: String) {
        if (!AppState.canAISpeakNow() || PublicSafetyLayer.isPaused()) return

        GlobalScope.launch {
            val reply = aiEngine.generateLiveResponse(speech, AppState.currentEmotion, AppState.currentContext)
            if (reply.shouldSpeak) voiceService.speak(reply.text, AppState.currentEmotion)
            memoryEngine.addMemory(MemoryItem(System.currentTimeMillis().toString(), "AI replied: ${reply.text}", 3))
            partnerEngine.maybePraiseUser()
        }
    }

    /* =========================================================
       Utility functions untuk nge-trigger semua dummy
    ========================================================== */
    fun triggerMotion() {
        motionAnalyzer.markMotionDetected()
    }

    fun triggerNotification(notif: WANotification) {
        notificationListener.handleNewNotification(notif)
    }

    fun triggerVoice(speech: String) {
        voiceEngine.attachSmartEyeXHooks()
        handleAIResponse(speech)
    }

    fun pauseAI(reason: String) {
        PublicSafetyLayer.pauseAI(reason)
    }

    fun resumeAI() {
        PublicSafetyLayer.resumeAI()
    }

    fun setUserBusy(busy: Boolean) {
        UserBoundaryManager.currentState = if (busy) UserBoundaryManager.UserState.BUSY else UserBoundaryManager.UserState.AVAILABLE
        AppState.userBusy = busy
    }
}