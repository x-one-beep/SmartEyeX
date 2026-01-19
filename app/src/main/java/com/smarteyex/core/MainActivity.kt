package com.smarteyex.core

import android.Manifest
import android.content.pm.PackageManager
import android.os.*
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.smarteyex.core.ai.GroqAIEngine
import com.smarteyex.core.camera.CameraFragment
import com.smarteyex.core.clock.ClockManager
import com.smarteyex.core.data.AppDatabase
import com.smarteyex.core.data.Event
import com.smarteyex.core.tts.TextToSpeechManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeechManager
    private lateinit var groqAI: GroqAIEngine
    private lateinit var clockManager: ClockManager

    private var lastMotionTime: Long = 0
    private val motionCooldown: Long = 3000

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            if (perms[Manifest.permission.CAMERA] == true &&
                perms[Manifest.permission.RECORD_AUDIO] == true
            ) startCamera()
            else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tts = TextToSpeechManager(this)
        groqAI = GroqAIEngine(this)
        clockManager = ClockManager(this, findViewById(R.id.tv_clock))

        Handler(Looper.getMainLooper()).postDelayed({
            findViewById<View>(R.id.splash_container).visibility = View.GONE
            findViewById<View>(R.id.start_container).visibility = View.VISIBLE
        }, 2000)

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            findViewById<View>(R.id.start_container).visibility = View.GONE
            findViewById<View>(R.id.dashboard_container).visibility = View.VISIBLE
            requestPermissionsIfNeeded()
            clockManager.start()
        }

        findViewById<Button>(R.id.btnToggleObserve).setOnClickListener {
            toggleCameraObserve()
        }

        findViewById<Button>(R.id.btnMemory).setOnClickListener {
            showMemory()
        }
    }

    private fun requestPermissionsIfNeeded() {
        val camOk = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val audOk = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!camOk || !audOk) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        } else startCamera()
    }

    private fun startCamera() {
        val frag = CameraFragment.newInstance()
        frag.setOnMotionDetectedListener {
            val now = SystemClock.elapsedRealtime()
            if (now - lastMotionTime >= motionCooldown) {
                lastMotionTime = now
                onMotionDetected()
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.camera_container, frag, "CAMERA")
            .commit()
    }

    private fun toggleCameraObserve() {
        val frag = supportFragmentManager.findFragmentByTag("CAMERA")
        if (frag != null)
            supportFragmentManager.beginTransaction().remove(frag).commit()
        else startCamera()
    }

    private fun onMotionDetected() {
        showFloatingHud("Movement Detected", "Analyzing...")
        tts.speak("Ada gerakan terdeteksi")

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)

            db.eventDao().insert(
                Event(
                    time = System.currentTimeMillis(),
                    type = "MOVEMENT",
                    data = "Motion detected"
                )
            )

            val aiResponse = groqAI.analyzeEvent("Movement detected")

            launch(Dispatchers.Main) {
                findViewById<TextView>(R.id.tv_ai_response)?.text = aiResponse
                tts.speak(aiResponse)
            }
        }
    }

    private fun showMemory() {
        lifecycleScope.launch(Dispatchers.IO) {
            val events = AppDatabase.getInstance(applicationContext)
                .eventDao()
                .getLastEvents(10)

            launch(Dispatchers.Main) {
                val msg = "Memory tersimpan: ${events.size}"
                showFloatingHud("Memory", msg)
                tts.speak(msg)
            }
        }
    }

    private fun showFloatingHud(title: String, message: String) {
        val container = findViewById<FrameLayout>(R.id.floatingHudContainer)
        container.removeAllViews()

        val view = LayoutInflater.from(this)
            .inflate(R.layout.view_floating_notification, container, false)

        view.findViewById<TextView>(R.id.hud_title).text = title
        view.findViewById<TextView>(R.id.hud_message).text = message
        container.addView(view)

        view.alpha = 0f
        view.scaleX = 0.9f
        view.scaleY = 0.9f

        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                view.postDelayed({
                    view.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction { container.removeAllViews() }
                        .start()
                }, 3000)
            }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
        clockManager.stop()
    }
}
