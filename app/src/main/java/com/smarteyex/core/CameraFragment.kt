package com.smarteyex.core

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView

class CameraFragment : Fragment() {

    private lateinit var status: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = TextView(requireContext())
        view.text = "Camera Ready (Stub)"
        status = view
        return view
    }

    fun updateStatus(text: String) {
        status.text = text
    }
}
