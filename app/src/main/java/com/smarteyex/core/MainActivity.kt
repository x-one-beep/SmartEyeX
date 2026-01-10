package com.smarteyex.core
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
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
 private var ttsManager: TextToSpeechManager? = null
 private var lastMotionTimestamp = 0L
 private val motionCooldownMs = 3000L
private val requestPermissions = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) { perms ->
    val cameraOk = perms[Manifest.permission.CAMERA] == true ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    if (cameraOk) startCameraFragment()
}

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    ttsManager = TextToSpeechManager(this)

    binding.btnToggleObserve.setOnClickListener {
        if (supportFragmentManager.findFragmentById(R.id.camera_container) == null) {
            startCameraFragment()
        } else {
            supportFragmentManager.beginTransaction()
                .remove(supportFragmentManager.findFragmentById(R.id.camera_container)!!)
                .commitAllowingStateLoss()
        }
    }

    binding.btnMemory.setOnClickListener {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            val events = db.eventDao().getLastEvents(10)
            runOnUiThread {
                showFloatingHud("Memory", if (events.isEmpty()) "No memory" else "Saved ${events.size} items")
            }
            ttsManager?.speak(if (events.isEmpty()) "Tidak ada memori" else "Tersimpan ${events.size} peristiwa")
        }
    }

    requestPermissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
}

override fun onDestroy() {
    super.onDestroy()
    ttsManager?.shutdown()
}

private fun startCameraFragment() {
    val frag = CameraFragment.newInstance()
    frag.setOnMotionDetectedListener {
        val now = SystemClock.elapsedRealtime()
        if (now - lastMotionTimestamp > motionCooldownMs) {
            lastMotionTimestamp = now
            onMotionDetected()
        }
    }
    supportFragmentManager.beginTransaction()
        .replace(R.id.camera_container, frag)
        .commitAllowingStateLoss()
}

private fun onMotionDetected() {
    showFloatingHud("Movement detected", "Analyzing...")
    ttsManager?.speak("Bung, ada gerakan terdeteksi. Saya sedang menganalisis.")
    lifecycleScope.launch(Dispatchers.IO) {
        val db = AppDatabase.getInstance(applicationContext)
        val e = Event(time = System.currentTimeMillis(), type = "MOVEMENT", data = "Motion detected by analyzer")
        db.eventDao().insert(e)
    }
}

private fun showFloatingHud(title: String, message: String) {
    val container = binding.floatingHudContainer
    container.removeAllViews()
    val view = LayoutInflater.from(this).inflate(R.layout.view_floating_notification, container, false)
    val titleView = view.findViewById<android.widget.TextView>(R.id.hud_title)
    val msgView = view.findViewById<android.widget.TextView>(R.id.hud_message)
    titleView.text = title
    msgView.text = message
    container.addView(view)
    view.scaleX = 0.92f
    view.scaleY = 0.92f
    view.alpha = 0f
    view.animate()
        .alpha(1f)
        .scaleX(1f)
        .scaleY(1f)
        .setDuration(300)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .withEndAction {
            view.postDelayed({
                view.animate().alpha(0f).scaleX(0.94f).scaleY(0.94f).setDuration(300)
                    .withEndAction { container.removeView(view) }
            }, 3000)
        }.start()
}
}
