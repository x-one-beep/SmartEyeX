package com.smarteyex.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class CameraFragment : Fragment() {

    private lateinit var controller: CameraController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return View(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        controller = CameraController(requireContext(), this)
        controller.start { frame ->
            VisionMemory.processFrame(frame)
        }

        AppSpeak.say("Kamera nyala. Mau nanya apa?")
    }

    override fun onDestroyView() {
        controller.stop()
        super.onDestroyView()
    }
}