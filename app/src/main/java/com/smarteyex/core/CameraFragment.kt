package com.smarteyex.core.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.fragment.app.Fragment
import com.smarteyex.app.R

class CameraFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var cameraController: CameraController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previewView = view.findViewById(R.id.cameraPreview)

        cameraController = CameraController(
            requireContext(),
            viewLifecycleOwner,
            previewView
        )

        cameraController.startCamera()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraController.stopCamera()
    }
}