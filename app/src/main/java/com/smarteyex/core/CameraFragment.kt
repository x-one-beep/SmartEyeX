package com.smarteyex.core

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment

class CameraFragment : Fragment() {

    private lateinit var controller: CameraController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_camera,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        controller = CameraController(
            requireContext(),
            viewLifecycleOwner
        )
        controller.startCamera()

        AppSpeak.say(
            "Kamera nyala. Tanya aja, gue liat."
        )
    }

    override fun onDestroyView() {
        controller.stopCamera()
        super.onDestroyView()
    }
}