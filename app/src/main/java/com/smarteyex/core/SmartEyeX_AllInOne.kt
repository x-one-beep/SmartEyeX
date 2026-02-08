package com.smarteyex.core

import android.app.*
import android.content.*
import android.os.*
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

/* =========================================================
   🫀 APPLICATION CORE
   ========================================================= */
class SmartEyeXApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SystemKernel.boot(this)
    }
}

/* =========================================================
   🚪 LAUNCHER (APK PASTI MUNCUL)
   ========================================================= */
class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this)
        tv.text = "SmartEyeX running"
        tv.textSize = 16f
        setContentView(tv)

        // Tidak memaksa, tidak crash
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
   ⚙️ SYSTEM KERNEL (JANTUNG SEBENARNYA)
   ========================================================= */
object SystemKernel {

    private val alive = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun boot(ctx: Context) {
        if (alive.getAndSet(true)) return

        AppContext.context = ctx.applicationContext
        LogBus.log("BOOT")

        Guard.run {
            hookX1()
            startLifecycles()
            startWatchdog()
            startMemoryLoop()
            startLearningLoop()
            startPersonaLoop()
            startPluginShell()
            startOTAShell()
        }
    }

    /* ---------- SAFE X1 HOOK (TIDAK WAJIB ADA) ---------- */
    private fun hookX1() {
        try {
            val cls = Class.forName("com.smarteyex.core.X1")
            cls.methods.forEach {
                if (it.name.contains("init", true)) {
                    it.invoke(null)
                }
            }
            LogBus.log("X1 hooked")
        } catch (_: Throwable) {
            LogBus.log("X1 not present, continue")
        }
    }

    /* ---------- BASIC LIFECYCLE ---------- */
    private fun startLifecycles() {
        Guard.run {
            SpeechOutput.init()
            SensorBridge.init()
            Dashboard.init()
        }
    }

    /* ---------- WATCHDOG (PASSIVE, NO RESTART FORCE) ---------- */
    private fun startWatchdog() {
        scope.launch {
            while (true) {
                delay(15_000)
                LogBus.log("Watchdog alive")
            }
        }
    }

    /* ---------- MEMORY CONSOLIDATION ---------- */
    private fun startMemoryLoop() {
        scope.launch(Dispatchers.IO) {
            while (true) {
                delay(30_000)
                MemoryEngine.compact()
            }
        }
    }

    /* ---------- SELF LEARNING (SAFE ADAPTIVE VARS) ---------- */
    private fun startLearningLoop() {
        scope.launch {
            while (true) {
                delay(10_000)
                AppState.learn()
            }
        }
    }

    /* ---------- PERSONA (PASSIVE SWITCH) ---------- */
    private fun startPersonaLoop() {
        scope.launch {
            while (true) {
                delay(5_000)
                PersonaEngine.evaluate()
            }
        }
    }

    /* ---------- PLUGIN SHELL (NO LOAD EXEC) ---------- */
    private fun startPluginShell() {
        PluginManager.init()
    }

    /* ---------- OTA SHELL (DISABLED BY DEFAULT) ---------- */
    private fun startOTAShell() {
        OTAEngine.prepare()
    }
}

/* =========================================================
   🛡️ CRASH GUARD
   ========================================================= */
object Guard {
    inline fun run(block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            LogBus.log("Guard: ${e.message}")
        }
    }
}

/* =========================================================
   🧠 STATE
   ========================================================= */
object AppState {
    var speechSpeed = 1.0f
    var emotion = 0.5f

    fun learn() {
        speechSpeed = (speechSpeed + 0.01f).coerceIn(0.8f, 1.3f)
    }
}

/* =========================================================
   🎭 PERSONA
   ========================================================= */
object PersonaEngine {
    fun evaluate() {
        LogBus.log("Persona check")
    }
}

/* =========================================================
   🧠 MEMORY
   ========================================================= */
object MemoryEngine {
    fun compact() {
        LogBus.log("Memory compact")
    }
}

/* =========================================================
   🔌 PLUGIN (SHELL ONLY)
   ========================================================= */
object PluginManager {
    fun init() {
        LogBus.log("Plugin shell ready")
    }
}

/* =========================================================
   🛰️ OTA (SHELL ONLY)
   ========================================================= */
object OTAEngine {
    fun prepare() {
        LogBus.log("OTA shell idle")
    }
}

/* =========================================================
   🔊 SPEECH (SAFE STUB)
   ========================================================= */
object SpeechOutput {
    fun init() {
        LogBus.log("Speech init")
    }
}

/* =========================================================
   📡 SENSOR BRIDGE
   ========================================================= */
object SensorBridge {
    fun init() {
        LogBus.log("Sensor bridge init")
    }
}

/* =========================================================
   📊 DASHBOARD
   ========================================================= */
object Dashboard {
    fun init() {
        LogBus.log("Dashboard init")
    }
}

/* =========================================================
   📍 APP CONTEXT HOLDER
   ========================================================= */
object AppContext {
    lateinit var context: Context
}

/* =========================================================
   🧾 LOGGER
   ========================================================= */
object LogBus {
    fun log(msg: String) {
        Log.d("SmartEyeX", msg)
    }
}