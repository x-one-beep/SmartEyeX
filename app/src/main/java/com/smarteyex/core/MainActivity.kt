package com.smarteyex.core

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.smarteyex.core.data.AppDatabase
import com.smarteyex.core.data.Event
import com.smarteyex.core.databinding.ActivityMainBinding
import com.smarteyex.core.tts.TextToSpeechManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var ttsManager: TextToSpeechManager

    private var lastMotionTime = 0L
    private val motionCooldownMs = 3000L

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val cameraGranted = perms[Manifest.permission.CAMERA] == true
        val audioGranted = perms[Manifest.permission.RECORD_AUDIO] == true

        if (cameraGranted && audioGranted) {
            startCameraFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ttsManager = TextToSpeechManager(this)

        binding.btnToggleObserve.setOnClickListener {
            val frag = supportFragmentManager.findFragmentByTag("CAMERA")
            if (frag == null) {
                startCameraFragment()
            } else {
                supportFragmentManager.beginTransaction()
                    .remove(frag)
                    .commit()
            }
        }

        binding.btnMemory.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getInstance(applicationContext)
                val events = db.eventDao().getLastEvents(10)

                runOnUiThread {
                    showFloatingHud(
                        "Memory",
                        if (events.isEmpty()) "No memory saved"
                        else "Saved ${events.size} items"
                    )
                }

                ttsManager.speak(
                    if (events.isEmpty()) "Tidak ada memori"
                    else "Tersimpan ${events.size} peristiwa"
                )
            }
        }

        requestPermissionsIfNeeded()
    }

    private fun requestPermissionsIfNeeded() {
        val cameraOk = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val audioOk = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!cameraOk || !audioOk) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        } else {
            startCameraFragment()
        }
    }

    private fun startCameraFragment() {
        val fragment = CameraFragment.newInstance().apply {
            setOnMotionDetectedListener {
                val now = SystemClock.elapsedRealtime()
                if (now - lastMotionTime >= motionCooldownMs) {
                    lastMotionTime = now
                    onMotionDetected()
                }
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.camera_container, fragment, "CAMERA")
            .commit()
    }

    private fun onMotionDetected() {
        showFloatingHud("Movement Detected", "Analyzing...")

        ttsManager.speak(
            "Bung, ada gerakan terdeteksi. Saya sedang menganalisis."
        )

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            db.eventDao().insert(
                Event(
                    time = System.currentTimeMillis(),
                    type = "MOVEMENT",
                    data = "Motion detected by camera analyzer"
                )
            )
        }
    }

    private fun showFloatingHud(title: String, message: String) {
        val container = binding.floatingHudContainer
        container.removeAllViews()

        val view = LayoutInflater.from(this)
            .inflate(R.layout.view_floating_notification, container, false)

        view.findViewById<TextView>(R.id.hud_title).text = title
        view.findViewById<TextView>(R.id.hud_message).text = message

        container.addView(view)

        view.apply {
            alpha = 0f
            scaleX = 0.92f
            scaleY = 0.92f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    postDelayed({
                        animate()
                            .alpha(0f)
                            .scaleX(0.94f)
                            .scaleY(0.94f)
                            .setDuration(300)
                            .withEndAction { container.removeView(this) }
                            .start()
                    }, 3000)
                }
                .start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
