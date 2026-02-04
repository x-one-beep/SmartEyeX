package com.smarteyex.core

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.camera2.CameraManager
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.*
import java.util.*
import android.speech.tts.TextToSpeech
import androidx.lifecycle.LifecycleService

/* --------------------------------------------
AppState — status global aplikasi
-------------------------------------------- */
object AppState {
    var isVoiceOn: Boolean = true
    var isListening: Boolean = true
    var batteryLow: Boolean = false
    var cpuHot: Boolean = false
    var userBusy: Boolean = false
    var isConversationCrowded: Boolean = false

    var currentEmotion: Emotion = Emotion.NEUTRAL
    var currentContext: String = "default"

    // Tambahan ekstrem: mode AI & userBusy manual
    var currentMode: String = "default"
    var lastUserInteraction: Long = System.currentTimeMillis()
}

enum class Emotion { NEUTRAL, HAPPY, SAD, ANGRY, OVERWHELMED }
fun Emotion.isOverwhelmed() = this == Emotion.OVERWHELMED

/* --------------------------------------------
MemoryEngine — ingatan selektif
-------------------------------------------- */
object MemoryEngine {
    private val memory = mutableListOf<MemoryItem>()
    fun addMemory(mem: MemoryItem) { memory.add(mem) }
    fun getRecentMemories(limit: Int = 10) = memory.takeLast(limit)
}

data class MemoryItem(
    val id: String,
    val text: String,
    val importance: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

/* --------------------------------------------
HardwareController — mic, camera, sensor
-------------------------------------------- */
object HardwareController {
    fun requestPermissions(activity: AppCompatActivity) {
        val perms = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.USE_BIOMETRIC
        )
        val toRequest = perms.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toRequest.isNotEmpty()) ActivityCompat.requestPermissions(activity, toRequest.toTypedArray(), 101)
    }
}

/* --------------------------------------------
VoiceService — AI voice pipeline
-------------------------------------------- */
class VoiceService(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech = TextToSpeech(context, this)

    fun speak(text: String, emotion: Emotion = Emotion.NEUTRAL) {
        if (!AppState.isVoiceOn || AppState.userBusy) return
        val prefix = when(emotion) {
            Emotion.HAPPY -> "Hehe, "
            Emotion.SAD -> "Hmmm, "
            Emotion.ANGRY -> "Aduh, "
            Emotion.OVERWHELMED -> ""
            else -> ""
        }
        tts.speak("$prefix$text", TextToSpeech.QUEUE_FLUSH, null, "SmartEyeXVoice")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("id", "ID")
            tts.setPitch(1.0f)
            tts.setSpeechRate(1.0f)
        }
    }
}

/* --------------------------------------------
SmartEyeXService — core background
-------------------------------------------- */
class SmartEyeXService : LifecycleService() {
    private lateinit var voiceService: VoiceService
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        voiceService = VoiceService(this)
        if (AppState.isListening) startListening()
    }

    private fun startListening() {
        coroutineScope.launch {
            while (AppState.isListening) {
                // Placeholder: integrate VoiceEngine + SpeechCommand + AI
                delay(500)
            }
        }
    }
}

/* --------------------------------------------
MainActivity — hub futuristik
-------------------------------------------- */
class MainActivity : AppCompatActivity() {
    private lateinit var voice: VoiceService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(generateMainUI())

        voice = VoiceService(this)
        HardwareController.requestPermissions(this)

        CoroutineScope(Dispatchers.Main).launch {
            voice.speak("Halo SmartEyeX! Sistem siap bekerja.", AppState.currentEmotion)
        }
    }

    private fun generateMainUI(): View {
        val root = FrameLayout(this)
        root.setBackgroundColor(Color.BLACK)

        val hud = TextView(this)
        hud.text = "SmartEyeX Dashboard"
        hud.setTextColor(Color.CYAN)
        hud.textSize = 24f
        hud.gravity = Gravity.CENTER

        // Tambahan ekstrem: shimmer HUD card
        val shimmer = LinearLayout(this)
        shimmer.setBackgroundColor(Color.DKGRAY)
        shimmer.alpha = 0.2f
        shimmer.gravity = Gravity.CENTER
        shimmer.addView(hud)

        root.addView(shimmer, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP
        ))

        return root
    }
}

/* --------------------------------------------
CameraActivity — Vision UI
-------------------------------------------- */
class CameraActivity : AppCompatActivity() {
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList.firstOrNull()
    }
}

/* --------------------------------------------
MemoryActivity — display memory AI
-------------------------------------------- */
class MemoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL

        val memView = TextView(this)
        memView.text = MemoryEngine.getRecentMemories().joinToString("\n") { it.text }
        memView.setTextColor(Color.MAGENTA)
        root.addView(memView)

        setContentView(root)
    }
}

/* --------------------------------------------
SettingsActivity — kendali user
-------------------------------------------- */
class SettingsActivity : AppCompatActivity() {
    private lateinit var voiceSwitch: Switch
    private lateinit var busySwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL

        voiceSwitch = Switch(this)
        voiceSwitch.text = "Voice ON/OFF"
        voiceSwitch.isChecked = AppState.isVoiceOn
        voiceSwitch.setOnCheckedChangeListener { _, isChecked -> AppState.isVoiceOn = isChecked }
        root.addView(voiceSwitch)

        // Tambahan ekstrem: toggle userBusy manual
        busySwitch = Switch(this)
        busySwitch.text = "User Busy"
        busySwitch.isChecked = AppState.userBusy
        busySwitch.setOnCheckedChangeListener { _, isChecked -> AppState.userBusy = isChecked }
        root.addView(busySwitch)

        setContentView(root)
    }
}