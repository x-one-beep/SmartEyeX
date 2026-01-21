package com.smarteyex.core

import android.content.Context
import android.widget.Toast

object VoiceRouter {

    private val parser = VoiceCommandParser()

    fun route(context: Context, text: String) {
        when (parser.parse(text)) {

            "OPEN_CAMERA" -> {
                NavigationStateManager.set(
                    NavigationStateManager.Screen.CAMERA
                )
                Toast.makeText(context, "Camera mode", Toast.LENGTH_SHORT).show()
            }

            "SHOW_TIME" -> {
                Toast.makeText(context, "Jam aktif", Toast.LENGTH_SHORT).show()
            }

            "ASK_AI" -> {
                Toast.makeText(context, "AI listening", Toast.LENGTH_SHORT).show()
            }

            "STOP_VOICE" -> {
                Toast.makeText(context, "Voice stopped", Toast.LENGTH_SHORT).show()
            }

            else -> {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
