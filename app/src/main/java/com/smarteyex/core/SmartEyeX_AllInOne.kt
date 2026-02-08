package com.smarteyex.core

import android.app.*
import android.content.*
import android.os.*
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

/* =========================================================
   🫀 APPLICATION
   ========================================================= */
class SmartEyeXApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SystemHeart.boot(this)
    }
}

/* =========================================================
   🚪 LAUNCHER ACTIVITY (APK MUNCUL)
   ========================================================= */
class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this)
        tv.text = "SmartEyeX aktif"
        tv.textSize = 18f
        setContentView(tv)

        safeOpen(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        safeOpen(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }

    private fun safeOpen(action: String) {
        try {
            startActivity(Intent(action))
        } catch (_: Throwable) {}
    }
}

/* =========================================================
   ❤️ SYSTEM HEART (ORKESTRATOR UTAMA)
   ========================================================= */
object SystemHeart {

    private val alive = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun boot(context: Context) {
        if (alive.getAndSet(true)) return

        AppContextHolder.context = context.applicationContext
        LogBus.log("SYSTEM BOOT")

        Guard.safe { SpeechOutput.init(context) }
        Guard.safe { SensorBrainIntegrator.init(context) }
        Guard.safe { SmartDashboard.init(context) }

        startPersonaPulse()
        startMemoryPulse()
        startLearningPulse()
        startWatchdog()
        startPluginShell()
        startOTAShell()
    }

    /* ================= WATCHDOG ================= */
    private fun startWatchdog() {
        scope.launch {
            while (true) {
                delay(15_000)
                LogBus.log("Watchdog alive")
            }
        }
    }

    /* ================= PERSONA ================= */
    private fun startPersonaPulse() {
        scope.launch {
            while (true) {
                delay(2_000)
                val ctx = PersonaEngine.analyzeContext(
                    AppState.currentSpeakerCount,
                    AppState.currentSpeechSpeed,
                    AppState.currentEmotionLevel,
                    AppState.keywordDetected,
                    AppState.userMentionedAI
                )
                LogBus.log("Persona tone: ${ctx.tone}")
            }
        }
    }

    /* ================= MEMORY ================= */
    private fun startMemoryPulse() {
        scope.launch(Dispatchers.IO) {
            while (true) {
                delay(30_000)
                SmartMemoryEngine(AppContextHolder.context).decayMemory()
            }
        }
    }

    /* ================= LEARNING ================= */
    private fun startLearningPulse() {
        scope.launch {
            while (true) {
                delay(10_000)
                AppState.currentSpeechSpeed =
                    (AppState.currentSpeechSpeed + 0.01f)
                        .coerceIn(0.8f, 1.5f)
            }
        }
    }

    /* ================= PLUGIN ================= */
    private fun startPluginShell() {
        LogBus.log("Plugin system ready (idle)")
    }

    /* ================= OTA ================= */
    private fun startOTAShell() {
        LogBus.log("OTA engine ready (idle)")
    }
}

/* =========================================================
   🛡️ GUARD
   ========================================================= */
object Guard {
    fun safe(block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            LogBus.log("Guard catch: ${e.message}")
        }
    }
}

/* =========================================================
   🧾 LOGGER
   ========================================================= */
object LogBus {
    fun log(msg: String) {
        Log.d("SmartEyeX", msg)
    }
}