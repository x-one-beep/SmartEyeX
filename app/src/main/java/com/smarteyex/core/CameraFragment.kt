package com.smarteyex.core
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.smarteyex.core.databinding.FragmentCameraBinding
import java.util.concurrent.Executors
class CameraFragment : Fragment() {
 private var binding: FragmentCameraBinding? = null
 private val binding get() = binding!!
 private val cameraExecutor = Executors.newSingleThreadExecutor()
 private var cameraProvider: ProcessCameraProvider? = null
 private var listener: (() -> Unit)? = null
fun setOnMotionDetectedListener(l: () -> Unit) { listener = l }

override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: android.os.Bundle?): View {
    _binding = FragmentCameraBinding.inflate(inflater, container, false)
    return binding.root
}

override fun onResume() {
    super.onResume()
    startCamera()
}

override fun onPause() {
    super.onPause()
    cameraProvider?.unbindAll()
}

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
    cameraExecutor.shutdown()
}

private fun startCamera() {
    val context = requireContext()
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        cameraProvider = cameraProviderFuture.get()
        bindCameraUseCases()
    }, ContextCompat.getMainExecutor(context))
}

@SuppressLint("UnsafeOptInUsageError")
private fun bindCameraUseCases() {
    val provider = cameraProvider ?: return
    provider.unbindAll()

    val preview = Preview.Builder()
        .setTargetRotation(binding.previewView.display.rotation)
        .build()
        .also {
            it.setSurfaceProvider(binding.previewView.surfaceProvider)
        }

    val analyzer = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
        .build()

    val motionAnalyzer = MotionAnalyzer(10.0, 1) {
        // callback on motion detected
        listener?.invoke()
    }
    analyzer.setAnalyzer(cameraExecutor, motionAnalyzer)

    val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
    try {
        provider.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview, analyzer)
    } catch (e: Exception) {
        Log.e("CameraFragment", "Bind failed: ${e.message}")
    }
}

companion object {
    fun newInstance() = CameraFragment()
}
}
