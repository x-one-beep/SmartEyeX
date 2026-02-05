package com.smarteyex.fullcore

import android.app.Notification
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.room.*
import kotlinx.coroutines.*
import java.util.*
import android.speech.tts.TextToSpeech
import android.content.Intent
import android.app.RemoteInput
import java.util.concurrent.atomic.AtomicBoolean
import java.util.Locale

/* ========================================
APP STATE & ENUMS
======================================== */
object AppState {
    var isListening = AtomicBoolean(false)
    var awaitingWaReply = false
    var lastWaNotification: StatusBarNotification? = null
    var lastSpokenText: String = ""
    var currentSpeakerCount: Int = 1
    var currentSpeechSpeed: Float = 1.0f
    var currentEmotionLevel: Int = 5
    var keywordDetected: Boolean = false
    var userMentionedAI: Boolean = false

    enum class Emotion { SAD, HAPPY, TIRED, ANGRY, EMPTY, CALM, CARING }
}

/* ========================================
MEMORY ENGINE
======================================== */

enum class MemoryType { CORE_IDENTITY, EMOTIONAL, SOCIAL, TEMPORAL }

@Entity(tableName = "memory_store")
data class MemoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: MemoryType,
    val summary: String,
    val emotion: String?,
    val relatedPerson: String?,
    val timestamp: Long,
    val importance: Int
)

@Dao
interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: MemoryEntity)

    @Query("SELECT * FROM memory_store WHERE importance >= :minImportance ORDER BY timestamp DESC")
    suspend fun getImportantMemories(minImportance: Int = 5): List<MemoryEntity>

    @Query("SELECT * FROM memory_store WHERE relatedPerson = :name")
    suspend fun getMemoriesByPerson(name: String): List<MemoryEntity>

    @Query("DELETE FROM memory_store WHERE importance <= :threshold")
    suspend fun forgetLowImportance(threshold: Int = 2)

    @Query("DELETE FROM memory_store")
    suspend fun wipeAll()
}

@Database(entities = [MemoryEntity::class], version = 1)
abstract class MemoryDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile private var INSTANCE: MemoryDatabase? = null
        fun get(context: Context): MemoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MemoryDatabase::class.java,
                    "smart_memory.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

class SmartMemoryEngine(context: Context) {
    private val dao = MemoryDatabase.get(context).memoryDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun remember(
        type: MemoryType,
        summary: String,
        emotion: String? = null,
        relatedPerson: String? = null,
        importance: Int = 5
    ) {
        scope.launch {
            dao.insert(
                MemoryEntity(
                    type = type,
                    summary = summary,
                    emotion = emotion,
                    relatedPerson = relatedPerson,
                    timestamp = System.currentTimeMillis(),
                    importance = importance
                )
            )
        }
    }

    suspend fun getEmotionalContext(): String? {
        val memories = dao.getImportantMemories()
        return memories.firstOrNull { it.type == MemoryType.EMOTIONAL }?.summary
    }

    suspend fun getPersonContext(name: String): String? {
        return dao.getMemoriesByPerson(name)
            .maxByOrNull { it.importance }
            ?.summary
    }

    fun decayMemory() {
        scope.launch { dao.forgetLowImportance() }
    }

    fun wipeMemory() {
        scope.launch { dao.wipeAll() }
    }
}

data class ShortMemory(
    val topic: String,
    val intent: String,
    val emotion: String,
    val timestamp: Long
)

object ShortMemoryStore {
    private val memories = mutableListOf<ShortMemory>()
    fun add(topic: String, intent: String, emotion: String) {
        memories.add(ShortMemory(topic, intent, emotion, System.currentTimeMillis()))
        if (memories.size > 10) memories.removeAt(0)
    }
    fun recent(): List<ShortMemory> = memories
}

/* ========================================
VOICE ENGINE
======================================== */

enum class VoiceEmotion { CALM, HAPPY, SAD, TIRED, ANGRY, CARING, SERIOUS }
enum class SpeechIntent { RESPOND, INFORM, INSTRUCT }

object SpeechOutput {
    private lateinit var tts: TextToSpeech
    fun init(ctx: Context) {
        tts = TextToSpeech(ctx) {
            tts.language = Locale("id", "ID")
            tts.setSpeechRate(0.9f)
        }
    }
    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "smarteyex_voice")
    }
}

class VoiceProsodyEngine {
    fun build(emotion: VoiceEmotion, intent: SpeechIntent): ProsodyProfile {
        return when (emotion) {
            VoiceEmotion.SAD -> ProsodyProfile(0.85f, 0.8f, 600, 1.0f)
            VoiceEmotion.HAPPY -> ProsodyProfile(1.1f, 1.05f, 250, 0.8f)
            VoiceEmotion.TIRED -> ProsodyProfile(0.9f, 0.75f, 700, 0.9f)
            VoiceEmotion.ANGRY -> ProsodyProfile(1.0f, 1.15f, 200, 0.2f)
            else -> ProsodyProfile(1.0f, 1.0f, 350, 0.6f)
        }
    }
}

data class ProsodyProfile(
    val pitch: Float,
    val speed: Float,
    val pauseMs: Int,
    val warmth: Float
)

class VoiceInputController(val context: Context) {
    fun startListening(onResult: (String) -> Unit) {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: return
                onResult(text)
            }
            override fun onError(error: Int) {}
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }
}

object CurhatEngine {
    fun respond(emotion: AppState.Emotion): String {
        return when (emotion) {
            AppState.Emotion.SAD -> "gue dengerin ya… lo nggak sendirian. pelan aja, nggak usah kuat-kuat."
            AppState.Emotion.TIRED -> "capek itu manusiawi. lo udah sejauh ini, itu aja udah keren."
            AppState.Emotion.ANGRY -> "marah tuh wajar. yang penting lo nggak nyakitin diri lo sendiri."
            AppState.Emotion.EMPTY -> "kalau lagi kosong, gue di sini. diem bareng juga gapapa."
            else -> "gue denger kok. lanjut aja ngomongnya."
        }
    }
}

object VoiceEngine {
    private lateinit var recognizer: SpeechRecognizer
    private lateinit var context: Context
    fun start(ctx: Context) {
        context = ctx
        recognizer = SpeechRecognizer.createSpeechRecognizer(ctx)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onResults(results: Bundle) {
                val spokenText = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: return
                AppState.lastSpokenText = spokenText
            }
        })
        listen()
    }
    private fun listen() {
        val intent = RecognizerIntent().apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        recognizer.startListening(intent)
        AppState.isListening.set(true)
    }
    fun stop() {
        recognizer.stopListening()
        AppState.isListening.set(false)
    }
}

/* ========================================
WA NOTIF & REPLY
======================================== */

object WhatsAppReplySender {
    fun sendReply(sbn: StatusBarNotification, reply: String) {
        val notification = sbn.notification
        val actions = notification.actions ?: return
        for (action in actions) {
            if (action.remoteInputs != null) {
                val intent = Intent()
                val bundle = Bundle()
                for (input in action.remoteInputs) {
                    bundle.putCharSequence(input.resultKey, reply)
                }
                RemoteInput.addResultsToIntent(action.remoteInputs, intent, bundle)
                action.actionIntent.send(null, 0, intent)
            }
        }
    }
}

class WaNotificationService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!sbn.packageName.contains("whatsapp")) return
        AppState.lastWaNotification = sbn
    }
}

class WaReplyAccessibilityService : AccessibilityService() {
    companion object { var instance: WaReplyAccessibilityService? = null
        fun sendReply(text: String) { instance?.replyToLatest(text) } }
    override fun onServiceConnected() { instance = this }
    private fun replyToLatest(text: String) {
        val root = rootInActiveWindow ?: return
        val replyButton = root.findAccessibilityNodeInfosByText("Reply").firstOrNull() ?: return
        replyButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        Handler(Looper.getMainLooper()).postDelayed({
            val input = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            val args = Bundle()
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            input?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            input?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }, 300)
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}

/* ========================================
SMART BRAIN
======================================== */

object SmartEyeXBrain {
    fun onWaMessageReceived(sender: String, message: String) {
        if (AppState.awaitingWaReply) return
    }
    fun onUserVoiceReply(text: String) {
        WaReplyAccessibilityService.sendReply(text)
    }
}

/* ========================================
PERSONA ENGINE
======================================== */

enum class PersonaMask { FRIENDLY, PARTNER, INSTRUCTOR, PARENT, ALERT }

data class PersonaContext(
    val mask: PersonaMask,
    val tone: String,
    val pitch: Float,
    val speed: Float,
    val warmth: Float
)

object PersonaEngine {
    private var currentMask: PersonaMask = PersonaMask.FRIENDLY
    private var microShiftLevel: Float = 0.0f

    fun analyzeContext(
        speakerCount: Int,
        speechSpeed: Float,
        emotionLevel: Int,
        keywordTrigger: Boolean,
        userMentionedAI: Boolean
    ): PersonaContext {
        microShiftLevel = (emotionLevel / 10f) + if (keywordTrigger) 0.1f else 0.0f
        currentMask = when {
            speakerCount > 3 -> PersonaMask.FRIENDLY
            speakerCount == 1 && userMentionedAI -> PersonaMask.PARTNER
            speechSpeed > 1.2f -> PersonaMask.INSTRUCTOR
            else -> PersonaMask.FRIENDLY
        }
        return PersonaContext(
            mask = currentMask,
            tone = when (currentMask) {
                PersonaMask.FRIENDLY -> "ringan"
                PersonaMask.PARTNER -> "hangat"
                PersonaMask.INSTRUCTOR -> "tegas"
                PersonaMask.PARENT -> "sopan"
                PersonaMask.ALERT -> "waspada"
            },
            pitch = 1.0f - microShiftLevel * 0.1f,
            speed = 1.0f + microShiftLevel * 0.1f,
            warmth = 0.5f + microShiftLevel * 0.5f
        )
    }
}

/* ========================================
CURHAT DEEP ENGINE
======================================== */

object CurhatDeepEngine {
    suspend fun respondToUser(userEmotion: AppState.Emotion): String {
        val memoryEngine = SmartMemoryEngine(AppContextHolder.context)
        val emotionalContext = memoryEngine.getEmotionalContext()
        val baseResponse = CurhatEngine.respond(userEmotion)
        return if (!emotionalContext.isNullOrEmpty()) {
            "$baseResponse gue inget juga sebelumnya lo pernah cerita soal: $emotionalContext"
        } else baseResponse
    }
}

/* ========================================
SMARTEYE X FULL BRAIN
======================================== */

object SmartEyeXFullBrain {
    private val personaEngine = PersonaEngine
    private val memoryEngine: SmartMemoryEngine by lazy { SmartMemoryEngine(AppContextHolder.context) }

    suspend fun processVoiceInput(spokenText: String) {
        val userEmotion = AppState.Emotion.SAD
        val personaCtx = personaEngine.analyzeContext(
            AppState.currentSpeakerCount,
            AppState.currentSpeechSpeed,
            AppState.currentEmotionLevel,
            AppState.keywordDetected,
            AppState.userMentionedAI
        )
        val curhatReply = CurhatDeepEngine.respondToUser(userEmotion)
        SpeechOutput.speak("[${personaCtx.tone}] $curhatReply")
    }

    fun handleWaMessage(sender: String, message: String) {
        AppState.lastWaNotification?.let {
            SmartEyeXBrain.onWaMessageReceived(sender, message)
        }
    }

    fun rememberEvent(summary: String, emotion: String?, relatedPerson: String?, importance: Int) {
        memoryEngine.remember(MemoryType.EMOTIONAL, summary, emotion, relatedPerson, importance)
    }
}

/* ========================================
APP CONTEXT HOLDER
======================================== */
object AppContextHolder {
    lateinit var context: Context
}

/* ========================================
EXAMPLE USAGE FLOW
======================================== */
fun exampleUsageFlow(ctx: Context) {
    AppContextHolder.context = ctx
    SpeechOutput.init(ctx)
    val memoryEngine = SmartMemoryEngine(ctx)
    memoryEngine.remember(MemoryType.CORE_IDENTITY, "User suka ngobrol malam hari", null, null, 6)
    VoiceEngine.start(ctx)
    SmartEyeXFullBrain.handleWaMessage("Zahra", "Halo, lagi sibuk ga?")
    GlobalScope.launch { SmartEyeXFullBrain.processVoiceInput("gue lagi sedih nih, capek banget") }
}