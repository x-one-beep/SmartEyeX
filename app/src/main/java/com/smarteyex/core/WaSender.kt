package com.smarteyex.core

import android.content.Intent
import android.net.Uri

class WaSender(private val context: android.content.Context = android.app.Application().applicationContext) {

    // Fungsi untuk send message via WA intent
    fun sendMessage(message: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/?text=${Uri.encode(message)}")
        }
        context.startActivity(intent)
    }
}