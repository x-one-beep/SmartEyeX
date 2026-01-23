package com.smarteyex.core.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class MotionAnalyzer : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {

        // 🔹 HOOK UNTUK:
        // - Face Detection
        // - Motion Detection
        // - Object Recognition
        // - SmartEye Vision AI

        // (sementara dummy agar aman & stabil)
        image.close()
    }
}