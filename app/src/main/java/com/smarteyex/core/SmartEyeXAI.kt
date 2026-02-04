package com.smarteyex.core

import android.content.Context
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue
import android.speech.tts.TextToSpeech


/* --------------------------------------------
VoiceEngine — capture suara & command
-------------------------------------------- */
class VoiceEngine(private val context: Context) {

    private var alwaysListening = false
    private var commandCallback: ((String) -> Unit)? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    // Tambahan ekstrem: sensitivitas adaptif & userBusy auto
    fun startListening(onSpeech: (String) -> Unit) {
        alwaysListening = true
        coroutineScope.launch {
            while (alwaysListening) {
                delay(500)
                val dummySpeech = "Halo SmartEyeX"
                
                // userBusy otomatis jika ada suara keras atau HP diam
                val now = System.currentTimeMillis()
                AppState.userBusy = (now - AppState.lastUserInteraction < 3000L).not() &&
                                     dummySpeech.isNotEmpty()

                onSpeech(dummySpeech)
            }
        }
    }

    fun startCommandListening(onCommand: (String) -> Unit) {
        commandCallback = onCommand
    }

    fun adjustSensitivity(emotion: Emotion, mode: String = "default", batteryLow: Boolean = false) {
        // adaptasi mic sensitivity berdasarkan state dan mode gila
    }
}

/* --------------------------------------------
GroqAiEngine — AI Core
-------------------------------------------- */
class GroqAiEngine(private val apiKey: String) {

    fun generateReply(input: String, emotion: Emotion, context: String): AIReply {
        val replyText = "AI saran: Balas '$input'"
        return AIReply(text = replyText)
    }

    fun generateLiveResponse(speech: String, emotion: Emotion, context: String): AIReply {
        val replyText = "AI live: '$speech'"
        return AIReply(text = replyText)
    }
}

data class AIReply(val text: String, val shouldSpeak: Boolean = true)

/* --------------------------------------------
SpeechCommand — handle voice command
-------------------------------------------- */
class SpeechCommand(
    private val appState: AppState,
    private val voiceEngine: VoiceEngine,
    private val voiceService: VoiceService,
    private val aiEngine: GroqAiEngine,
    private val memoryManager: MemoryEngine
) {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    fun startListening() {
        voiceEngine.startCommandListening { command ->
            if (!canAiReact()) return@startCommandListening
            coroutineScope.launch { handleCommand(command) }
        }
    }

    private fun canAiReact(): Boolean {
        if (!appState.isVoiceOn) return false
        if (appState.userBusy) return false
        if (appState.currentEmotion.isOverwhelmed()) return false
        return true
    }

    private suspend fun handleCommand(command: String) {
        memoryManager.addMemory(MemoryItem(System.currentTimeMillis().toString(), "User command: $command", 3))
        when {
            command.contains("ngobrol", ignoreCase = true) -> {
                val resp = aiEngine.generateLiveResponse(command, appState.currentEmotion, appState.currentContext)
                if (resp.shouldSpeak) voiceService.speak(resp.text, appState.currentEmotion)
            }
            command.contains("cek memori", ignoreCase = true) -> {
                val memSummary = memoryManager.getRecentMemories().joinToString { it.text }
                voiceService.speak("Memori terbaru: $memSummary", appState.currentEmotion)
            }
            command.contains("balas WA", ignoreCase = true) -> {
                voiceService.speak("Siap, gue bantu balas WA.", appState.currentEmotion)
            }
            else -> {
                val aiReply = aiEngine.generateReply(command, appState.currentEmotion, appState.currentContext)
                if (aiReply.shouldSpeak) voiceService.speak(aiReply.text, appState.currentEmotion)
            }
        }
    }
}

/* --------------------------------------------
MotionAnalyzer — sensor / camera hook
-------------------------------------------- */
class MotionAnalyzer(private val context: Context, private val memoryManager: MemoryEngine) {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    fun startMonitoring() {
        coroutineScope.launch {
            while (true) {
                delay(1000)
                val detected = false
                if (detected) {
                    memoryManager.addMemory(MemoryItem(System.currentTimeMillis().toString(), "Motion detected", 2))
                }
            }
        }
    }
}

/* --------------------------------------------
WAAccessibility — baca notif WA
-------------------------------------------- */
class WAAccessibility(
    private val context: Context,
    private val appState: AppState,
    private val voiceService: VoiceService,
    private val memoryManager: MemoryEngine
) {

    var replyManager: WAReplyManager? = null
    var notificationListener: NotificationListener? = null

    fun readLatestNotification() {
        val latest = notificationListener?.getLatestWANotification() ?: return
        memoryManager.addMemory(MemoryItem(System.currentTimeMillis().toString(), "WA: ${latest.text}", 3))
        voiceService.speak("WA baru dari ${latest.sender}: ${latest.text}", appState.currentEmotion)
        replyManager?.queueMessage(latest.text)
    }

    fun sendMessage(phone: String, message: String) {
        if (!canAiReply()) return
        replyManager?.sendMessageDirect(message)
        memoryManager.addMemory(MemoryItem(System.currentTimeMillis().toString(), "WA replied: $message", 3))
    }

    private fun canAiReply(): Boolean {
        if (appState.userBusy) return false
        if (appState.currentEmotion.isOverwhelmed()) return false
        return true
    }
}

data class WANotification(val text: String, val sender: String, val timestamp: Long)

/* --------------------------------------------
WAReplyManager — queue & send WA
-------------------------------------------- */
class WAReplyManager(
    private val appState: AppState,
    private val voiceService: VoiceService,
    private val memoryManager: MemoryEngine,
    private val aiEngine: GroqAiEngine
) {

    private val messageQueue = ConcurrentLinkedQueue<String>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    fun queueMessage(text: String) {
        messageQueue.add(text)
        processQueue()
    }

    private fun processQueue() {
        coroutineScope.launch {
            while (messageQueue.isNotEmpty()) {
                val incoming = messageQueue.poll() ?: continue
                if (!canAiReply()) continue
                val aiReply = aiEngine.generateReply(incoming, appState.currentEmotion, appState.currentContext)
                voiceService.speak("WA: \"$incoming\" saran balas: \"${aiReply.text}\"", appState.currentEmotion)
                val userApproved = true
                if (userApproved) sendMessageDirect(aiReply.text)
                memoryManager.addMemory(MemoryItem(System.currentTimeMillis().toString(), "WA queued reply: ${aiReply.text}", 3))
            }
        }
    }

    fun sendMessageDirect(message: String) {
        println("Sending WA message: $message")
    }

    private fun canAiReply(): Boolean {
        if (appState.userBusy) return false
        if (appState.currentEmotion.isOverwhelmed()) return false
        return true
    }
}

/* --------------------------------------------
NotificationListener — notif sistem
-------------------------------------------- */
class NotificationListener(
    private val appState: AppState,
    private val voiceService: VoiceService,
    private val memoryManager: MemoryEngine,
    private val groqAiEngine: GroqAiEngine,
    private val waAccessibility: WAAccessibility,
    private val waReplyManager: WAReplyManager
) {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var onNewNotificationCallback: ((WANotification) -> Unit)? = null

    fun setOnNewNotification(callback: (WANotification) -> Unit) { onNewNotificationCallback = callback }

    fun handleNewNotification(notif: WANotification) {
        coroutineScope.launch {
            if (appState.userBusy || appState.currentEmotion.isOverwhelmed()) return@launch
            memoryManager.addMemory(MemoryItem(System.currentTimeMillis().toString(), "Notif: ${notif.sender} -> ${notif.text}", 3))
            voiceService.speak("Notif dari ${notif.sender}: ${notif.text}", appState.currentEmotion)
            if (notif.sender.startsWith("+") || notif.sender.contains("WhatsApp")) {
                waAccessibility.readLatestNotification()
                waReplyManager.queueMessage(notif.text)
            }
            onNewNotificationCallback?.invoke(notif)
        }
    }

    fun getLatestWANotification(): WANotification? {
        return null
    }
}