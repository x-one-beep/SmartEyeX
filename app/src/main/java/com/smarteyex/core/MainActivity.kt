package com.smarteyex.core

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.smarteyex.core.ai.GroqAiEngine
import com.smarteyex.core.clock.ClockManager
import com.smarteyex.core.memory.MemoryManager
import com.smarteyex.core.navigation.NavigationStateManager
import com.smarteyex.core.voice.VoiceEngine
import com.smarteyex.core.voice.VoiceService

class MainActivity : AppCompatActivity() {

    // UI
    private lateinit var txtClock: TextView
    private lateinit var btnCamera: ImageButton
    private lateinit var btnAI: ImageButton
    private lateinit var btnMemory: ImageButton
    private lateinit var btnVoice: ImageButton
    private lateinit var btnWA: ImageButton
    private lateinit var btnSetting: ImageButton

    // Core Engine
    private lateinit var clockManager: ClockManager
    private lateinit var aiEngine: GroqAiEngine
    private lateinit var memoryManager: MemoryManager
    private lateinit var voiceEngine: VoiceEngine
    private lateinit var navigation: NavigationStateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()
        initCore()
        initAction()
        startBackgroundService()
    }

    private fun initUI() {
        txtClock = findViewById(R.id.txtClock)

        btnCamera = findViewById(R.id.btnCamera)
        btnAI = findViewById(R.id.btnAI)
        btnMemory = findViewById(R.id.btnMemory)
        btnVoice = findViewById(R.id.btnVoice)
        btnWA = findViewById(R.id.btnWA)
        btnSetting = findViewById(R.id.btnSetting)
    }

    private fun initCore() {
        clockManager = ClockManager(txtClock)
        aiEngine = GroqAiEngine(this)
        memoryManager = MemoryManager(this)
        voiceEngine = VoiceEngine(this)
        navigation = NavigationStateManager(this)

        clockManager.start()
    }

    private fun initAction() {

        btnCamera.setOnClickListener {
            navigation.openCamera()
        }

        btnAI.setOnClickListener {
            navigation.openAI()
        }

        btnMemory.setOnClickListener {
            navigation.openMemory()
        }

        btnVoice.setOnClickListener {
            voiceEngine.toggleListening()
        }

        btnWA.setOnClickListener {
            navigation.openWA()
        }

        btnSetting.setOnClickListener {
            navigation.openSetting()
        }
    }

    private fun startBackgroundService() {
        val intent = Intent(this, VoiceService::class.java)
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        clockManager.stop()
    }
}