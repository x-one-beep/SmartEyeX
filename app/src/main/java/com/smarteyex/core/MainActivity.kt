package com.smarteyex.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.smarteyex.core.ClockManager
import com.smarteyex.core.GroqAiEngine
import com.smarteyex.core.VoiceEngine
import com.smarteyex.app.camera.CameraController
import com.smarteyex.app.memory.MemoryManager
import com.smarteyex.app.smart.SmartMode
import com.smarteyex.app.wa.WaNotificationListener
import com.smarteyex.app.util.PermissionHelper
import java.util.*

class MainActivity : AppCompatActivity() {

    // ===== UI =====
    private lateinit var txtClock: TextView
    private lateinit var txtDate: TextView
    private lateinit var statusCamera: TextView
    private lateinit var statusSound: TextView
    private lateinit var statusAI: TextView
    private lateinit var miniHud: TextView
    private lateinit var etChat: EditText
    private lateinit var btnMic: ImageButton
    private lateinit var btnSend: ImageButton
    private lateinit var chatContainer: LinearLayout
    private lateinit var btnDashboard: Button
    private lateinit var btnCamera: Button
    private lateinit var btnMemory: Button
    private lateinit var btnSetting: Button

    // ===== CORE =====
    private lateinit var clockManager: ClockManager
    private lateinit var groqAi: GroqAiEngine
    private lateinit var voiceEngine: VoiceEngine
    private lateinit var cameraController: CameraController
    private lateinit var memoryManager: MemoryManager
    private lateinit var smartMode: SmartMode

    // ===== CONFIG =====
    private val PERMISSION_REQ = 1308
    private val MASTER_COMMAND = "SmartEyeX 130809"
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ===== INIT UI =====
        txtClock = findViewById(R.id.tvClock)
        txtDate = findViewById(R.id.tvDate)
        statusCamera = findViewById(R.id.statusCamera)
        statusSound = findViewById(R.id.statusSound)
        statusAI = findViewById(R.id.statusAI)
        miniHud = findViewById(R.id.miniHud)
        etChat = findViewById(R.id.etChat)
        btnMic = findViewById(R.id.btnMic)
        btnSend = findViewById(R.id.btnSend)
        chatContainer = findViewById(R.id.chatContainer)
        btnDashboard = findViewById(R.id.btnDashboard)
        btnCamera = findViewById(R.id.btnCamera)
        btnMemory = findViewById(R.id.btnMemory)
        btnSetting = findViewById(R.id.btnSetting)

        // ===== PERMISSIONS =====
        PermissionHelper.requestAll(this, PERMISSION_REQ)

        // ===== INIT CORE =====
        clockManager = ClockManager()
        groqAi = GroqAiEngine()
        voiceEngine = VoiceEngine(this)
        cameraController = CameraController(this)
        memoryManager = MemoryManager(this)
        smartMode = SmartMode(this, groqAi, memoryManager, voiceEngine)

        // ===== START CLOCK =====
        clockManager.start { time, date ->
            txtClock.text = time
            txtDate.text = date
        }

        // ===== START CAMERA =====
        cameraController.startCamera()
        statusCamera.text = "🎥 ON"

        // ===== START WA LISTENER =====
        startService(Intent(this, WaNotificationListener::class.java))

        // ===== INIT AI STATUS =====
        statusAI.text = "🧠 READY"

        // ===== BUTTON NAVIGATION =====
        btnDashboard.setOnClickListener { showDashboard() }
        btnCamera.setOnClickListener { showCamera() }
        btnMemory.setOnClickListener { showMemory() }
        btnSetting.setOnClickListener { showSettings() }

        // ===== CHAT MIC & SEND =====
        btnMic.setOnClickListener {
            voiceEngine.startListening { command ->
                handleCommand(command)
            }
        }

        btnSend.setOnClickListener {
            val text = etChat.text.toString()
            if (text.isNotBlank()) {
                handleCommand(text)
                etChat.text.clear()
            }
        }
    }

    // ===== HANDLE COMMAND =====
    private fun handleCommand(command: String) {
        if (command.equals(MASTER_COMMAND, ignoreCase = true)) {
            voiceEngine.speak("Akses memori dibuka")
            showMemory()
            return
        }

        // Simpan memori
        memoryManager.save(command)

        // Proses AI
        groqAi.ask(command, onResult = { response ->
            runOnUiThread {
                statusAI.text = response.take(20) + "..."
                addChat("AI: $response")
                voiceEngine.speak(response)
            }
        }, onError = { err ->
            runOnUiThread {
                statusAI.text = "AI ERROR"
                addChat("AI ERROR: $err")
            }
        })

        addChat("User: $command")
    }

    // ===== UI HELPERS =====
    private fun addChat(text: String) {
        val tv = TextView(this)
        tv.text = text
        tv.setTextColor(0xFFFFFFFF.toInt())
        chatContainer.addView(tv)
        handler.post { scrollChatToBottom() }
    }

    private fun scrollChatToBottom() {
        (chatContainer.parent as? ScrollView)?.fullScroll(View.FOCUS_DOWN)
    }

    private fun showDashboard() {
        cameraController.hideCameraPreview()
        statusCamera.text = "🎥 OFF"
        chatContainer.visibility = View.VISIBLE
    }

    private fun showCamera() {
        cameraController.showCameraPreview()
        statusCamera.text = "🎥 ON"
        chatContainer.visibility = View.GONE
    }

    private fun showMemory() {
        cameraController.hideCameraPreview()
        statusCamera.text = "🎥 OFF"
        chatContainer.removeAllViews()
        chatContainer.visibility = View.VISIBLE
        val memDump = memoryManager.dumpMemory()
        addChat("Memory:\n$memDump")
    }

    private fun showSettings() {
        cameraController.hideCameraPreview()
        statusCamera.text = "🎥 OFF"
        chatContainer.visibility = View.GONE
        Toast.makeText(this, "Settings belum aktif", Toast.LENGTH_SHORT).show()
    }

    // ===== PERMISSION CALLBACK =====
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ) {
            for (res in grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission wajib!", Toast.LENGTH_LONG).show()
                    finish()
                    return
                }
            }
        }
    }

    // ===== CLEANUP =====
    override fun onDestroy() {
        super.onDestroy()
        cameraController.stopCamera()
        voiceEngine.shutdown()
        clockManager.stop()
    }
}
