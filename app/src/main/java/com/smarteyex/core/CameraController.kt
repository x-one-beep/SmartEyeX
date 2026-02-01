package com.smarteyex.core

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

class CameraController(private val context: Context, private val lifecycleOwner: LifecycleOwner) {

    private var camera: Camera? = null

    // Fungsi untuk start kamera dengan HUD overlay
    fun startCamera(surfaceProvider: androidx.camera.core.Preview.SurfaceProvider) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Fungsi untuk stop kamera
    fun stopCamera() {
        camera?.cameraControl?.cancelFocusAndMetering()
    }
}