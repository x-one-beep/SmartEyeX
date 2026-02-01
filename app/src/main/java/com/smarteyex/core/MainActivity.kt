package com.smarteyex.core

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.smarteyex.core.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var clockManager: ClockManager
    private lateinit var appState: AppState
    private lateinit var navigationStateManager: NavigationStateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi komponen utama
        clockManager = ClockManager(this, binding.clockText)
        appState = AppState()
        navigationStateManager = NavigationStateManager()

        // Load Camera HUD
        val cameraFragment = CameraFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.camera_container, cameraFragment)
            .commit()

        // Setup AI Chat Panel
        binding.aiSend.setOnClickListener {
            val message = binding.aiInput.text.toString()
            if (message.isNotEmpty()) {
                GroqAiEngine().chatWithAI(message) { response ->
                    VoiceEngine().speak(response)  // TTS response
                }
            }
        }

        // Start background services
        SmartEyeXService.startService(this)
    }

    override fun onResume() {
        super.onResume()
        clockManager.startClock()
    }

    override fun onPause() {
        super.onPause()
        clockManager.stopClock()
    }
}