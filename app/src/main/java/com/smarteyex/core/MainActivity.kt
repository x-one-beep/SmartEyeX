package com.smarteyex.core

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.smarteyex.app.R
import com.smarteyex.core.ai.GroqAiEngine
import com.smarteyex.core.clock.ClockManager
import com.smarteyex.core.memory.MemoryManager
import com.smarteyex.core.navigation.NavigationStateManager

class MainActivity : AppCompatActivity() {

    // UI
    private lateinit var txtClock: TextView
    private lateinit var btnCamera: ImageButton
    private lateinit var btnAI: ImageButton
    private lateinit var btnMemory: ImageButton
    private lateinit var btnVoice: ImageButton
    private lateinit var btnWA: ImageButton
    private lateinit var btnSetting: ImageButton

    // Core
    private lateinit var clockManager: ClockManager
    private lateinit var aiEngine: GroqAiEngine
    private lateinit var memoryManager: MemoryManager
    private lateinit var navigation: NavigationStateManager

    private val REQ_AUDIO = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()
        initCore()
        initAction()
        checkPermissionAndStartService()
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
        navigation = NavigationStateManager(this)
        clockManager.start()
    }

    private fun initAction() {
        btnCamera.setOnClickListener { navigation.openCamera() }
        btnAI.setOnClickListener { navigation.openAI() }
        btnMemory.setOnClickListener { navigation.openMemory() }

        // Tombol voice cuma indikator / manual restart service
        

btnVoice.setOnClickListener {
    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
        == PackageManager.PERMISSION_GRANTED
    ) {
        voiceEngine.startListening() // pakai method startListening() di VoiceEngine
    } else {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.RECORD_AUDIO),
            REQ_AUDIO
        )
    }

        btnWA.setOnClickListener { navigation.openWA() }
        btnSetting.setOnClickListener { navigation.openSetting() }
    }

    private fun checkPermissionAndStartService() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startVoiceService()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQ_AUDIO
            )
        }
    }

    private fun startVoiceService() {
        startService(Intent(this, VoiceService::class.java))
    }

    private fun restartVoiceService() {
        stopService(Intent(this, VoiceService::class.java))
        startVoiceService()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_AUDIO &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startVoiceService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clockManager.stop()
    }
}