package com.smarteyex.fullcore

import android.Manifest
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
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.room.*
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.log10
import kotlin.math.sqrt

/* ======================================== APP CONTEXT HOLDER ======================================== */
object AppContextHolder {
    lateinit var context: Context
}

/* ======================================== APP STATE & ENUMS ======================================== */
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

    enum class Emotion { SAD, HAPPY, TIRED, ANGRY, EMPTY, CALM, CARING }
}

/* ======================================== MEMORY ENGINE ======================================== */
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

        fun get(context: Context): MemoryDatabase =
            INSTANCE ?: synchronized(this) {
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

/* ======================================== SMART MEMORY ENGINE ======================================== */
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
            try {
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getEmotionalContext(): String? =
        dao.getImportantMemories().firstOrNull { it.type == MemoryType.EMOTIONAL }?.summary

    suspend fun getPersonContext(name: String): String? =
        dao.getMemoriesByPerson(name).maxByOrNull { it.importance }?.summary

    fun decayMemory() = scope.launch { dao.forgetLowImportance() }
    fun wipeMemory() = scope.launch { dao.wipeAll() }
}

/* ======================================== SHORT MEMORY ======================================== */
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

/* ======================================== VOICE ENGINE ======================================== */
enum class VoiceEmotion { CALM, HAPPY, SAD, TIRED, ANGRY, CARING, SERIOUS }
enum class SpeechIntent { RESPOND, INFORM, INSTRUCT }

object SpeechOutput {
    private lateinit var tts: TextToSpeech
    private var isReady = false

    fun init(ctx: Context) {
        tts = TextToSpeech(ctx) {
            tts.language = Locale("id", "ID")
            tts.setSpeechRate(0.9f)
            isReady = true
        }
    }

    fun speak(text: String) {
        if (!isReady) return
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, "smarteyex_voice")
    }
}

class VoiceProsodyEngine {
    fun build(emotion: VoiceEmotion, intent: SpeechIntent): ProsodyProfile =
        when (emotion) {
            VoiceEmotion.SAD -> ProsodyProfile(0.85f, 0.8f, 600, 1.0f)
            VoiceEmotion.HAPPY -> ProsodyProfile(1.1f, 1.05f, 250, 0.8f)
            VoiceEmotion.TIRED -> ProsodyProfile(0.9f, 0.75f, 700, 0.9f)
            VoiceEmotion.ANGRY -> ProsodyProfile(1.0f, 1.15f, 200, 0.2f)
            else -> ProsodyProfile(1.0f, 1.0f, 350, 0.6f)
        }
}

data class ProsodyProfile(
    val pitch: Float,
    val speed: Float,
    val pauseMs: Int,
    val warmth: Float
)

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

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
    )
    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
}

recognizer?.startListening(intent)
        AppState.isListening.set(true)
    }

    fun stopListening() {
        recognizer?.stopListening()
        recognizer?.destroy()
        AppState.isListening.set(false)
    }
}

/* ======================================== CAMERA & FACE EMOTION ENGINE ======================================== */
object CameraSensorEngine {
    private lateinit var context: Context
    private var cameraProvider: ProcessCameraProvider? = null

    fun init(ctx: Context) {
        context = ctx
        val future = ProcessCameraProvider.getInstance(ctx)
        future.addListener(
            { cameraProvider = future.get() },
            ContextCompat.getMainExecutor(ctx)
        )
    }

    fun startFaceDetection(onFaceDetected: (Face) -> Unit) {
        val provider = cameraProvider ?: return

        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(options)

        analyzer.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )
                detector.process(image)
                    .addOnSuccessListener { faces ->
                        faces.firstOrNull()?.let { onFaceDetected(it) }
                    }
                    .addOnCompleteListener { imageProxy.close() }
            } else imageProxy.close()
        }

        try {
            provider.unbindAll()
            if (context is LifecycleOwner) {
                provider.bindToLifecycle(
                    context as LifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    analyzer
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/* ======================================== AMBIENT NOISE SENSOR ======================================== */
object AmbientNoiseSensor {
    private var audioRecord: AudioRecord? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isRecording = false

    fun startSampling(onDbLevel: (Float) -> Unit) {
        if (isRecording) return

        val bufferSize = AudioRecord.getMinBufferSize(
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()
        isRecording = true

        scope.launch {
            val buffer = ShortArray(bufferSize)
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    val rms =
                        sqrt(buffer.take(read).map { it.toDouble() * it }.average())
                    val db =
                        if (rms > 0) 20 * log10(rms / 32768.0) else 0.0
                    onDbLevel(db.toFloat())
                }
                delay(200)
            }
        }
    }

    fun stopSampling() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}

/* ======================================== PROXIMITY SENSOR ENGINE ======================================== */
class ProximitySensorEngine(
    context: Context,
    private val onChange: (Boolean) -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximitySensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    fun start() {
        proximitySensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val isNear =
            event.values[0] < (proximitySensor?.maximumRange ?: 5f)
        onChange(isNear)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

/* ======================================== SENSOR → BRAIN INTEGRATION ======================================== */
object SensorBrainIntegrator {

    fun init(context: Context) {
        CameraSensorEngine.init(context)

        AmbientNoiseSensor.startSampling { db ->
            AppState.currentSpeechSpeed =
                (1.0f + db / 100f).coerceIn(0.8f, 1.5f)
            updatePersona()
        }

        CameraSensorEngine.startFaceDetection { face ->
            AppState.currentEmotionLevel =
                when {
                    face.smilingProbability ?: 0f > 0.7f -> 8
                    face.smilingProbability ?: 0f < 0.3f -> 3
                    else -> 5
                }
            updatePersona()
        }
    }

    fun setProximity(isNear: Boolean) {
        if (isNear) {
            val ctx = PersonaEngine.analyzeContext(
                AppState.currentSpeakerCount,
                AppState.currentSpeechSpeed,
                AppState.currentEmotionLevel,
                AppState.keywordDetected,
                AppState.userMentionedAI
            )
            SpeechOutput.speak("[${ctx.tone}] Aku deteksi kamu dekat.")
        }
    }

    private fun updatePersona() {
        val ctx = PersonaEngine.analyzeContext(
            AppState.currentSpeakerCount,
            AppState.currentSpeechSpeed,
            AppState.currentEmotionLevel,
            AppState.keywordDetected,
            AppState.userMentionedAI
        )
        println(
            "Persona updated: tone=${ctx.tone}, pitch=${ctx.pitch}, speed=${ctx.speed}"
        )
    }

    fun stopAll() {
        AmbientNoiseSensor.stopSampling()
    }
}

/* ======================================== PERSONA ENGINE ======================================== */
object PersonaEngine {

    data class PersonaContext(
        val mask: String = "default",
        val tone: String = "calm",
        val pitch: Float = 1f,
        val speed: Float = 1f,
        val warmth: Float = 0.6f
    )

    fun analyzeContext(
        speakerCount: Int,
        speed: Float,
        emotionLevel: Int,
        keywordDetected: Boolean,
        userMentionedAI: Boolean
    ): PersonaContext {
        return PersonaContext(
            mask = "personaMask",
            tone = if (emotionLevel > 6) "happy" else "calm",
            pitch = 1f,
            speed = speed,
            warmth = 0.7f
        )
    }
}

/* ======================================== CURHAT ENGINE ======================================== */
object CurhatEngine {
    fun respond(emotion: AppState.Emotion): String =
        when (emotion) {
            AppState.Emotion.HAPPY -> "Senang mendengarnya!"
            AppState.Emotion.SAD -> "Aku ngerti perasaanmu..."
            AppState.Emotion.TIRED -> "Istirahat yang cukup ya."
            else -> "Aku di sini buatmu."
        }
}

/* ======================================== WHATSAPP ENGINE ======================================== */
object WhatsAppReplySender {
    fun sendReply(sbn: StatusBarNotification, reply: String) {
        val actions = sbn.notification.actions ?: return
        for (action in actions) {
            val inputs = action.remoteInputs ?: continue
            val intent = Intent()
            val bundle = Bundle()
            for (input in inputs) {
                bundle.putCharSequence(input.resultKey, reply)
            }
            RemoteInput.addResultsToIntent(inputs, intent, bundle)
            try {
                action.actionIntent.send(null, 0, intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

object WaReplyEngine {
    fun reply(sbn: StatusBarNotification, reply: String) {
        WhatsAppReplySender.sendReply(sbn, reply)
    }
}

class WaNotificationService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!sbn.packageName.contains("whatsapp")) return
        val extras = sbn.notification.extras
        val sender =
            extras.getString(Notification.EXTRA_TITLE) ?: return
        val message =
            extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                ?: return

        AppState.lastWaNotification = sbn
        SmartEyeXBrain.onWaMessageReceived(sender, message)
    }
}

class WaReplyAccessibilityService : AccessibilityService() {

    companion object {
        var instance: WaReplyAccessibilityService? = null
        fun sendReply(text: String) {
            instance?.replyToLatest(text)
        }
    }

    override fun onServiceConnected() {
        instance = this
    }

    private fun replyToLatest(text: String) {
        val root = rootInActiveWindow ?: return
        val reply =
            root.findAccessibilityNodeInfosByText("Reply")
                .firstOrNull() ?: return

        reply.performAction(AccessibilityNodeInfo.ACTION_CLICK)

        Handler(Looper.getMainLooper()).postDelayed({
            val input =
                root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            val args = Bundle()
            args.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
            input?.performAction(
                AccessibilityNodeInfo.ACTION_SET_TEXT,
                args
            )
        }, 300)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}

/* ======================================== SMART DASHBOARD ======================================== */
object SmartDashboard {

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun init(ctx: Context) {
        setup()
        startParticles()
    }

    private fun setup() {
        println("Dashboard ready")
    }

    private fun startParticles() {
        scope.launch {
            while (true) {
                println("Memory shards: ${ShortMemoryStore.recent().size}")
                delay(500)
            }
        }
    }

    fun displayAROverlay(
        bitmap: Bitmap,
        label: String,
        x: Float,
        y: Float
    ) {
        println("AR Overlay $label at $x,$y")
    }
}

/* ======================================== MULTI MODAL ENGINE ======================================== */
object MultiModalEngine {

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun handleVoiceInput(text: String) {
        scope.launch {
            SmartEyeXFullBrain.processVoiceInput(text)
        }
    }

    fun handleTextInput(text: String) {
        handleVoiceInput(text)
    }
}

/* ======================================== SMART BRAIN ======================================== */
object SmartEyeXBrain {

    fun onWaMessageReceived(sender: String, message: String) {
        SpeechOutput.speak("Pesan dari $sender: ${message.take(80)}")
        SmartEyeXFullBrain.rememberEvent(
            "Pesan WA dari $sender: $message",
            null,
            sender,
            5
        )
    }
}

object CurhatDeepEngine {
    suspend fun respondToUser(
        emotion: AppState.Emotion
    ): String {
        val engine =
            SmartMemoryEngine(AppContextHolder.context)
        val context = engine.getEmotionalContext()
        val base = CurhatEngine.respond(emotion)
        return if (!context.isNullOrEmpty())
            "$base Gue inget lo pernah cerita soal $context"
        else base
    }
}

/* ======================================== FULL BRAIN ======================================== */
object SmartEyeXFullBrain {

    private val memoryEngine by lazy {
        SmartMemoryEngine(AppContextHolder.context)
    }

    suspend fun processVoiceInput(text: String) {
        val emotion = detectEmotion(text)
        val reply =
            CurhatDeepEngine.respondToUser(emotion)
        SpeechOutput.speak(reply)

        if (AppState.awaitingWaReply &&
            AppState.lastWaNotification != null
        ) {
            WaReplyEngine.reply(
                AppState.lastWaNotification!!,
                text
            )
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

    private fun detectEmotion(text: String): AppState.Emotion =
        when {
            text.contains("senang", true) ->
                AppState.Emotion.HAPPY
            text.contains("sedih", true) ->
                AppState.Emotion.SAD
            text.contains("capek", true) ->
                AppState.Emotion.TIRED
            else -> AppState.Emotion.CALM
        }
}