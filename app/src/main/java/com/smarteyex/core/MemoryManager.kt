package com.smarteyex.core

import android.content.Context
import android.content.SharedPreferences

class MemoryManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("SmartEyeX", Context.MODE_PRIVATE)

    // Fungsi untuk simpan percakapan
    fun saveConversation(conversation: String) {
        prefs.edit().putString("conversation", conversation).apply()
    }

    // Fungsi untuk load percakapan
    fun loadConversation(): String = prefs.getString("conversation", "") ?: ""

    // Fungsi untuk simpan data wajah/suara (placeholder)
    fun saveFaceData(data: String) {
        prefs.edit().putString("face_data", data).apply()
    }
}