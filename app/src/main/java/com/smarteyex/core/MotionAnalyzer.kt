package com.smarteyex.core
import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlin.math.abs

class MotionAnalyzer(
 private val threshold: Double = 10.0,
 private val consecutiveRequired: Int = 1,
 private val onMotion: () -> Unit
) : ImageAnalysis.Analyzer {  private var lastLumaAvg: Double? = null
 private var stableCount = 0  override fun analyze(image: ImageProxy) {
 try {
 val format = image.format
 if (format != ImageFormat.YUV_420_888 && format != ImageFormat.YV12) {
 image.close()
 return
 }  val buffer = image.planes[0].buffer
    val data = ByteArray(buffer.remaining())
    buffer.get(data)
    var sum = 0L
    for (b in data) sum += (b.toInt() and 0xFF)
    val avg = sum.toDouble() / data.size

    val last = lastLumaAvg
    if (last != null) {
        val diff = abs(avg - last)
        if (diff > threshold) {
            stableCount++
            if (stableCount >= consecutiveRequired) {
                try {
                    onMotion.invoke()
                } catch (t: Throwable) {
                    Log.w("MotionAnalyzer", "onMotion threw: ${t.message}")
                }
                stableCount = 0
            }
        } else {
            stableCount = 0
        }
    }
    lastLumaAvg = avg
} catch (e: Exception) {
    Log.e("MotionAnalyzer", "analyze error: ${e.message}")
} finally {
    image.close()
}  }
}
