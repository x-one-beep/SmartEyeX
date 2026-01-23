package com.smarteyex.core.memory

import android.content.Context
import android.content.SharedPreferences

class MemoryManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("SmartEyeXMemory", Context.MODE_PRIVATE)

    fun save(key: String, value: String) {
        val timestamp = System.currentTimeMillis()
        prefs.edit()
            .putString("$key-$timestamp", value)
            .apply()
    }

    fun getAll(): Map<String, *> {
        return prefs.all
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}