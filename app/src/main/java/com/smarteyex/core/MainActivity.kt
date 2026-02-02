package com.smarteyex.core

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smarteyex.core.chat.ChatActivity
import com.smarteyex.core.camera.CameraActivity
import com.smarteyex.core.memory.MemoryActivity
import com.smarteyex.core.settings.SettingsActivity
import com.smarteyex.core.state.AppState
import com.smarteyex.core.service.SmartEyeXService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var appState: AppState
    private val uiHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appState = (application as SmartEyeXApp).appState

        startCoreService()
        observeGlobalState()
        initClock()
        initNavigation()
    }

    /**
     * ===============================
     * CORE SERVICE
     * ===============================
     * AI hidup walau user cuma di beranda
     */
    private fun startCoreService() {
        val intent = Intent(this, SmartEyeXService::class.java)
        startService(intent)
    }

    /**
     * ===============================
     * GLOBAL STATE OBSERVER
     * ===============================
     * MainActivity TIDAK mikir,
     * dia cuma mantau & tampilkan status AI
     */
    private fun observeGlobalState() {
        lifecycleScope.launch {
            appState.stateFlow.collect { state ->

                // 1️⃣ Status AI (awake / rest)
                updateAiPresence(state.isAwake)

                // 2️⃣ Mode (normal / school / game)
                updateModeIndicator(state.currentMode)

                // 3️⃣ Emosi dominan (ringkas)
                updateEmotionIndicator(state.emotion)

                // 4️⃣ Mic status (privacy aware)
                updateMicIndicator(state.micMode)
            }
        }
    }

    /**
     * ===============================
     * JAM = DENYUT HIDUP AI
     * ===============================
     * Dipakai AI untuk:
     * - gaya bicara
     * - timing respon
     */
    private fun initClock() {
        val clockRunnable = object : Runnable {
            override fun run() {
                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                findViewById<android.widget.TextView>(R.id.tvTime).text = time
                uiHandler.postDelayed(this, 1000)
            }
        }
        uiHandler.post(clockRunnable)
    }

    /**
     * ===============================
     * NAVIGASI = INDERA AI
     * ===============================
     */
    private fun initNavigation() {

        findViewById<View>(R.id.btnChat).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        findViewById<View>(R.id.btnCamera).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        findViewById<View>(R.id.btnMemory).setOnClickListener {
            startActivity(Intent(this, MemoryActivity::class.java))
        }

        findViewById<View>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    /**
     * ===============================
     * UI UPDATE SECTION
     * ===============================
     * Semua transparan → trust user
     */

    private fun updateAiPresence(isAwake: Boolean) {
        // contoh: icon glow hidup / redup
        // AI hadir tapi ga selalu ngomong
    }

    private fun updateModeIndicator(mode: AppState.Mode) {
        // Normal / School / Game
        // efek visual beda, perilaku AI beda
    }

    private fun updateEmotionIndicator(emotion: AppState.Emotion) {
        // ringkas: ikon / warna
        // detail emosi dipakai AI, bukan UI
    }

    private fun updateMicIndicator(micMode: AppState.MicMode) {
        // Always listening / Trigger / Off
        // PRIVASI JELAS
    }

    override fun onDestroy() {
        super.onDestroy()
        uiHandler.removeCallbacksAndMessages(null)
    }
}