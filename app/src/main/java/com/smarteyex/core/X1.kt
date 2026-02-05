package com.smarteyex.core

/* =============================== IMPORTS =============================== */

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.room.*

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.log10
import kotlin.math.sqrt

/* =============================== APP CONTEXT =============================== */

object AppContextHolder {
    lateinit var context: Context
}

/* =============================== APP STATE =============================== */

object AppState {
    var isListening = AtomicBoolean(false)
    var awaitingWaReply = false
    var lastWaNotification: StatusBarNotification? = null
    var lastSpokenText: String = ""
    var currentSpeakerCount = 1
    var currentSpeechSpeed = 1.0f
    var currentEmotionLevel = 5
    var keywordDetected = false
    var userMentionedAI = false

    enum class Emotion {
        SAD, HAPPY, TIRED, ANGRY, EMPTY, CALM, CARING
    }
}

/* =============================== MEMORY ENGINE =============================== */

enum class MemoryType {
    CORE_IDENTITY, EMOTIONAL, SOCIAL, TEMPORAL
}

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
                ).fallbackToDestructiveMigration()
                 .build()
                 .also { INSTANCE = it }
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
        emotion: String?,
        relatedPerson: String?,
        importance: Int
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

    suspend fun getEmotionalContext(): String? =
        dao.getImportantMemories()
            .firstOrNull { it.type == MemoryType.EMOTIONAL }
            ?.summary
}

/* =============================== SHORT MEMORY =============================== */

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

    fun recent(): List<ShortMemory> = memories.toList()
}

/* =============================== VOICE ENGINE =============================== */

enum class VoiceEmotion { CALM, HAPPY, SAD, TIRED, ANGRY, CARING, SERIOUS }
enum class SpeechIntent { RESPOND, INFORM, INSTRUCT }

object SpeechOutput {
    private lateinit var tts: TextToSpeech
    private var ready = false

    fun init(ctx: Context) {
        tts = TextToSpeech(ctx) {
            tts.language = Locale("id", "ID")
            tts.setSpeechRate(0.9f)
            ready = true
        }
    }

    fun speak(text: String) {
        if (!ready) return
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, "smarteyex_voice")
    }
}

class VoiceInputController(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var recognizer: SpeechRecognizer? = null

    fun startListening(onResult: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        if (recognizer == null)
            recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val text =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull() ?: return
                scope.launch { onResult(text) }
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

        val intent = RecognizerIntent().apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
        }

        recognizer?.startListening(intent)
        AppState.isListening.set(true)
    }

    fun stopListening() {
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
        AppState.isListening.set(false)
    }
}

/* =============================== CAMERA & FACE =============================== */

object CameraSensorEngine {

    private lateinit var context: Context
    private var provider: ProcessCameraProvider? = null

    fun init(ctx: Context) {
        context = ctx
        val future = ProcessCameraProvider.getInstance(ctx)
        future.addListener(
            { provider = future.get() },
            ContextCompat.getMainExecutor(ctx)
        )
    }

    fun startFaceDetection(onFaceDetected: (Face) -> Unit) {
        val cameraProvider = provider ?: return

        val analyzer = ImageAnalysis.Builder().build()

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()

        val detector = FaceDetection.getClient(options)

        analyzer.setAnalyzer(
            ContextCompat.getMainExecutor(context)
        ) { proxy: ImageProxy ->
            val media = proxy.image
            if (media != null) {
                val img =
                    InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
                detector.process(img)
                    .addOnSuccessListener { faces ->
                        faces.firstOrNull()?.let(onFaceDetected)
                    }
                    .addOnCompleteListener { proxy.close() }
            } else proxy.close()
        }

        if (context is LifecycleOwner) {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                context as LifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                analyzer
            )
        }
    }
}

/* =============================== AMBIENT NOISE =============================== */

object AmbientNoiseSensor {

    private var recorder: AudioRecord? = null
    private var running = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun startSampling(onDb: (Float) -> Unit) {
        if (running) return

        val bufferSize = AudioRecord.getMinBufferSize(
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        recorder?.startRecording()
        running = true

        scope.launch {
            val buffer = ShortArray(bufferSize)
            while (running) {
                val read = recorder?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    val rms =
                        sqrt(buffer.take(read).map { it * it.toDouble() }.average())
                    val db = 20 * log10(rms / 32768.0)
                    onDb(db.toFloat())
                }
                delay(200)
            }
        }
    }

    fun stop() {
        running = false
        recorder?.stop()
        recorder?.release()
        recorder = null
    }
}

/* =============================== PERSONA ENGINE =============================== */

object PersonaEngine {

    data class PersonaContext(
        val mask: String,
        val tone: String,
        val pitch: Float,
        val speed: Float,
        val warmth: Float
    )

    fun analyzeContext(
        speakerCount: Int,
        speed: Float,
        emotionLevel: Int,
        keywordDetected: Boolean,
        userMentionedAI: Boolean
    ): PersonaContext {
        return PersonaContext(
            mask = "default",
            tone = if (emotionLevel > 6) "happy" else "calm",
            pitch = 1.0f,
            speed = speed,
            warmth = 0.7f
        )
    }
}

/* =============================== WHATSAPP =============================== */

class WaNotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!sbn.packageName.contains("whatsapp")) return
        val extras = sbn.notification.extras
        val sender = extras.getString(Notification.EXTRA_TITLE) ?: return
        val msg =
            extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return
        AppState.lastWaNotification = sbn
        SmartEyeXBrain.onWaMessageReceived(sender, msg)
    }
}

class WaReplyAccessibilityService : AccessibilityService() {

    companion object {
        var instance: WaReplyAccessibilityService? = null
        fun sendReply(text: String) {
            instance?.reply(text)
        }
    }

    override fun onServiceConnected() {
        instance = this
    }

    private fun reply(text: String) {
        val root = rootInActiveWindow ?: return
        val replyBtn =
            root.findAccessibilityNodeInfosByText("Reply").firstOrNull() ?: return
        replyBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)

        Handler(Looper.getMainLooper()).postDelayed({
            val input = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            val args = Bundle()
            args.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
            input?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }, 300)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}

/* =============================== FULL BRAIN =============================== */

object SmartEyeXFullBrain {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    suspend fun processVoiceInput(text: String) {
        val persona = PersonaEngine.analyzeContext(
            AppState.currentSpeakerCount,
            AppState.currentSpeechSpeed,
            AppState.currentEmotionLevel,
            AppState.keywordDetected,
            AppState.userMentionedAI
        )
        SpeechOutput.speak("[${persona.tone}] Aku dengar: $text")
    }
}

object SmartEyeXBrain {

    fun onWaMessageReceived(sender: String, message: String) {
        SpeechOutput.speak("Pesan dari $sender")
        AppState.awaitingWaReply = true
    }
}

/* ===============================
SMART DASHBOARD & UI
=============================== */

object SmartDashboard {

    private lateinit var context: Context
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun init(ctx: Context) {
        context = ctx
        setupHolographicUI()
        startMemoryParticles()
        bindNotifications()
    }

    private fun setupHolographicUI() {
        println("Dashboard initialized: holographic shards moving, interactive buttons ready")
    }

    private fun startMemoryParticles() {
        scope.launch {
            while (true) {
                val count = ShortMemoryStore.recent().size
                println("Memory particles updated: $count shards")
                delay(500)
            }
        }
    }

    private fun bindNotifications() {
        println("Notification feed bound to dashboard")
    }

    fun displayAROverlay(bitmap: Bitmap, label: String, x: Float, y: Float) {
        println("AR Overlay: $label at ($x,$y)")
    }

    fun scrollToFeature(featureName: String) {
        println("Scrolling dashboard to feature: $featureName")
    }

    fun triggerUserFeedback(text: String) {
        SpeechOutput.speak(text)
        println("User feedback triggered: $text")
    }
}

/* ===============================
MULTI-MODAL INTERACTION ENGINE
=============================== */

object MultiModalEngine {

    fun handleVoiceInput(text: String) {
        scopeLaunch { SmartEyeXFullBrain.processVoiceInput(text) }
    }

    fun handleTextInput(text: String) {
        println("Text input received: $text")
        scopeLaunch { SmartEyeXFullBrain.processVoiceInput(text) }
    }

    fun handleGesture(action: String) {
        println("Gesture detected: $action")
    }

    fun handleVisual(face: Face?, bitmap: Bitmap?) {
        if (face != null) {
            AppState.currentEmotionLevel = when {
                face.smilingProbability ?: 0f > 0.7f -> 8
                face.smilingProbability ?: 0f < 0.3f -> 3
                else -> 5
            }
        }
        if (bitmap != null) {
            SmartDashboard.displayAROverlay(bitmap, "Detected Object", 100f, 200f)
        }
    }

    private fun scopeLaunch(block: suspend () -> Unit) {
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            block()
        }
    }
}

/* ===============================
VOICE & CHAT FLOW
=============================== */

object SmartConversation {

    fun userSpeaks(text: String) {
        println("User says: $text")
        MultiModalEngine.handleVoiceInput(text)
    }

    fun userTextInput(text: String) {
        println("User text input: $text")
        MultiModalEngine.handleTextInput(text)
    }

    fun aiInterjectRandom() {
        val randomResponses = listOf(
            "Eh, tau ga sih? Lagi trending nih!",
            "Haha, lucu juga ya itu!",
            "Hati-hati ya, jangan sampe salah langkah.",
            "Aku inget lo cerita soal ini sebelumnya..."
        )
        SpeechOutput.speak(randomResponses.random())
    }
}

/* ===============================
CURHAT DEEP ENGINE
=============================== */

object CurhatDeepEngine {

    suspend fun respondToUser(userEmotion: AppState.Emotion): String {
        val memoryEngine = SmartMemoryEngine(AppContextHolder.context)
        val emotionalContext = memoryEngine.getEmotionalContext()
        val baseResponse = CurhatEngine.respond(userEmotion)

        return if (!emotionalContext.isNullOrEmpty())
            "$baseResponse Gue inget juga sebelumnya lo cerita soal: $emotionalContext"
        else baseResponse
    }
}

/* ===============================
SMART FULL BRAIN – FINAL
=============================== */

object SmartEyeXFullBrain {

    private val personaEngine = PersonaEngine
    private val memoryEngine by lazy {
        SmartMemoryEngine(AppContextHolder.context)
    }

    suspend fun processVoiceInput(spokenText: String) {
        val userEmotion = detectEmotionFromContext(spokenText)

        val personaCtx = personaEngine.analyzeContext(
            AppState.currentSpeakerCount,
            AppState.currentSpeechSpeed,
            AppState.currentEmotionLevel,
            AppState.keywordDetected,
            AppState.userMentionedAI
        )

        val reply = CurhatDeepEngine.respondToUser(userEmotion)
        SpeechOutput.speak("[${personaCtx.tone}] $reply")

        if (AppState.awaitingWaReply && AppState.lastWaNotification != null) {
            WaReplyEngine.reply(AppState.lastWaNotification!!, spokenText)
            SpeechOutput.speak("Udah gue kirim ya.")
            AppState.awaitingWaReply = false
        }
    }

    fun rememberEvent(
        summary: String,
        emotion: String?,
        relatedPerson: String?,
        importance: Int
    ) {
        memoryEngine.remember(
            MemoryType.EMOTIONAL,
            summary,
            emotion,
            relatedPerson,
            importance
        )
    }

    private fun detectEmotionFromContext(text: String): AppState.Emotion =
        when {
            text.contains("senang", true) -> AppState.Emotion.HAPPY
            text.contains("sedih", true) -> AppState.Emotion.SAD
            text.contains("capek", true) -> AppState.Emotion.TIRED
            else -> AppState.Emotion.CALM
        }
}