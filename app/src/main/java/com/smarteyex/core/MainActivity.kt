package com.smarteyex.core

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.smarteyex.core.state.AppState
import com.smarteyex.core.service.SmartEyeXService
import com.smarteyex.core.voice.VoiceService
import com.smarteyex.core.chat.ChatActivity
import com.smarteyex.core.camera.CameraActivity
import com.smarteyex.core.memory.MemoryActivity
import com.smarteyex.core.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var clockView: TextView
    private lateinit var dashboardContainer: LinearLayout
    private lateinit var featureScroll: HorizontalScrollView
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize global services
        VoiceService.init(this)
        startService(SmartEyeXService::class.java)

        // Bind UI elements
        clockView = findViewById(R.id.textClock)
        dashboardContainer = findViewById(R.id.dashboardContainer)
        featureScroll = findViewById(R.id.featureScroll)

        initClock()
        initDashboard()
        initFeatureButtons()
        initTouchEffects()
    }

    private fun initClock() {
        // Update neon clock setiap detik
        handler.post(object : Runnable {
            override fun run() {
                val currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                clockView.text = currentTime
                // Neon glow animation bisa di XML / drawable
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun initDashboard() {
        // Tampilkan WA notif, cuaca, status AI
        // Misal notif WA belum dibaca
        val notifText = TextView(this).apply {
            text = "WA: 3 unread, Cuaca: 28°C, AI: Awake"
            setTextColor(android.graphics.Color.CYAN)
            textSize = 16f
            // Bisa ditambah animasi neon / glow
        }
        dashboardContainer.addView(notifText)
    }

    private fun initFeatureButtons() {
        // Tombol fitur scroll horizontal di bawah dashboard
        val chatBtn = createFeatureButton("Chat") { startActivity(Intent(this, ChatActivity::class.java)) }
        val cameraBtn = createFeatureButton("Camera") { startActivity(Intent(this, CameraActivity::class.java)) }
        val memoryBtn = createFeatureButton("Memory") { startActivity(Intent(this, MemoryActivity::class.java)) }
        val settingsBtn = createFeatureButton("Settings") { startActivity(Intent(this, SettingsActivity::class.java)) }

        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(chatBtn)
            addView(cameraBtn)
            addView(memoryBtn)
            addView(settingsBtn)
        }

        featureScroll.addView(buttonContainer)
    }

    private fun createFeatureButton(label: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            textSize = 14f
            setPadding(40, 20, 40, 20)
            setTextColor(android.graphics.Color.WHITE)
            background = resources.getDrawable(R.drawable.neon_button_bg, null)
            setOnClickListener { onClick() }
            // Tambah animasi glow / transparan sesuai style
        }
    }

    private fun initTouchEffects() {
        // Bisa inject NeonTouchLayer di root layout untuk ripple/particle per fitur
        // Efek sentuh aktif, fade cepat, hemat daya
    }

    private fun startService(service: Class<*>) {
        val intent = Intent(this, service)
        startService(intent)
    }
}