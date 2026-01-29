package com.smarteyex.core.memory

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class MemoryManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("SmartEyeXMemoryCore", Context.MODE_PRIVATE)

    /* =======================
       1. MEMORY PERCAKAPAN
       ======================= */
    fun saveConversation(from: String, text: String) {
        val time = System.currentTimeMillis()
        val obj = JSONObject().apply {
            put("from", from)
            put("text", text)
            put("time", time)
        }

        val arr = getJsonArray("conversations")
        arr.put(obj)
        saveArray("conversations", arr)
    }

    fun getConversations(): JSONArray =
        getJsonArray("conversations")

    /* =======================
       2. GAYA BICARA USER
       ======================= */
    fun saveSpeakingStyle(style: String) {
        prefs.edit().putString("user_speaking_style", style).apply()
    }

    fun getSpeakingStyle(): String =
        prefs.getString("user_speaking_style", "netral") ?: "netral"

    /* =======================
       3. PROFIL MANUSIA (SUARA)
       ======================= */
    fun registerHumanVoice(name: String, voicePrint: String) {
        val humans = getJsonArray("human_voices")

        val obj = JSONObject().apply {
            put("name", name)
            put("voice", voicePrint)
        }

        humans.put(obj)
        saveArray("human_voices", humans)
    }

    fun identifySpeaker(voicePrint: String): String? {
        val humans = getJsonArray("human_voices")
        for (i in 0 until humans.length()) {
            val obj = humans.getJSONObject(i)
            if (obj.getString("voice") == voicePrint) {
                return obj.getString("name")
            }
        }
        return null
    }

    /* =======================
       4. UTIL
       ======================= */
    private fun getJsonArray(key: String): JSONArray {
        val raw = prefs.getString(key, null)
        return if (raw != null) JSONArray(raw) else JSONArray()
    }

    private fun saveArray(key: String, array: JSONArray) {
        prefs.edit().putString(key, array.toString()).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}