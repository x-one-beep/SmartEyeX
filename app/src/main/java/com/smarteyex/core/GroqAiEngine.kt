package com.smarteyex.core.ai

import android.content.Context

class GroqAIEngine(private val context: Context) {

    fun analyzeEvent(input: String): String {
        // Placeholder logic Gen-Z style
        return when {
            input.contains("Movement", true) -> "Oke Bung, gerakan terdeteksi nih!"
            input.contains("WA", true) -> "Bung, ada WA masuk, cek HP ya!"
            else -> "Siap Bung, lagi standby..."
        }
    }

}
