package com.smarteyex.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.smarteyex.core.databinding.FragmentCameraBinding

class CameraFragment : Fragment() {

    private lateinit var binding: FragmentCameraBinding
    private lateinit var cameraController: CameraController
    private lateinit var motionAnalyzer: MotionAnalyzer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraController = CameraController(requireContext(), viewLifecycleOwner)
        motionAnalyzer = MotionAnalyzer()

        // Start kamera dengan HUD
        cameraController.startCamera(binding.cameraView.surfaceProvider)

        // Analisis gerakan untuk trigger AI
        motionAnalyzer.startAnalysis { motionDetected ->
            if (motionDetected) {
                GroqAiEngine().generateRandomResponse { response ->
                    VoiceEngine().speak(response)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraController.stopCamera()
        motionAnalyzer.stopAnalysis()
    }
}