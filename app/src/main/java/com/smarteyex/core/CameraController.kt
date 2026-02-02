package com.smarteyex.core

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class CameraController(
    private val context: Context,
    private val lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {

    private val executor = Executors.newSingleThreadExecutor()
    private var imageAnalysis: ImageAnalysis? = null

    fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(context)

        providerFuture.addListener({
            val provider = providerFuture.get()

            val preview = Preview.Builder().build()

            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(
                    ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                )
                .build()
                .also {
                    it.setAnalyzer(executor) { image ->
                        VisionEngine.analyze(image)
                        image.close()
                    }
                }

            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )

        }, ContextCompat.getMainExecutor(context))
    }

    fun stopCamera() {
        imageAnalysis?.clearAnalyzer()
    }
}
