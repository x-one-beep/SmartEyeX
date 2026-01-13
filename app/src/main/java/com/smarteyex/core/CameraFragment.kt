package com.smarteyex.core

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.smarteyex.core.databinding.FragmentCameraBinding
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var listener: (() -> Unit)? = null

    fun setOnMotionDetectedListener(l: () -> Unit) {
        listener = l
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            cameraProvider = future.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(context))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        val provider = cameraProvider ?: return
        provider.unbindAll()

        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analyzer.setAnalyzer(
            cameraExecutor,
            MotionAnalyzer(10.0, 1) {
                listener?.invoke()
            }
        )

        val selector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            provider.bindToLifecycle(
                viewLifecycleOwner,
                selector,
                preview,
                analyzer
            )
        } catch (e: Exception) {
            Log.e("CameraFragment", "Bind failed", e)
        }
    }

    companion object {
        fun newInstance() = CameraFragment()
    }
}
