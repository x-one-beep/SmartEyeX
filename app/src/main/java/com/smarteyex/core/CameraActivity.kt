package com.smarteyex.core.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Base64
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.smarteyex.core.R
import com.smarteyex.core.ai.GroqAiEngine
import com.smarteyex.core.BaseNeonActivity
import com.smarteyex.core.NeonTouchLayer

class CameraActivity : BaseNeonActivity(), SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private lateinit var touchLayer: NeonTouchLayer
    private lateinit var cameraController: CameraController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val rootLayout = findViewById<FrameLayout>(R.id.cameraRoot)
        surfaceView = findViewById(R.id.cameraSurface)

        // Inject touch effect layer
        touchLayer = NeonTouchLayer(this, NeonTouchLayer.EffectType.CAMERA_FOCUS)
        rootLayout.addView(touchLayer)

        // Request camera permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        } else {
            initCamera()
        }
    }

    private fun initCamera() {
        cameraController = CameraController(this, surfaceView)
        surfaceView.holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        cameraController.startPreview()
        startFrameProcessing()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        cameraController.stopPreview()
    }

    private fun startFrameProcessing() {
        cameraController.setFrameListener { frameBitmap ->
            // Convert bitmap to Base64 if needed
            val byteArrayOutputStream = java.io.ByteArrayOutputStream()
            frameBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
            val base64Frame = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)

            // Send frame to AI engine (GroqAiEngine)
            GroqAiEngine.processCameraFrame(frameBitmap) { aiResponse ->
                runOnUiThread {
                    // Optional: overlay AI response on HUD
                    touchLayer.showOverlayText(aiResponse)
                }
            }
        }
    }
}