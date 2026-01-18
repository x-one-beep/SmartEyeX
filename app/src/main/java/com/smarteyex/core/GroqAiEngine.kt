package com.smarteyex.core.ai

import android.content.Context
import android.util.Log
import com.smarteyex.core.BuildConfig
import com.smarteyex.core.data.AppDatabase
import com.smarteyex.core.data.Event
import com.smarteyex.core.tts.TextToSpeechManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class GroqAiEngine(
    private val context: Context,
    private val tts: TextToSpeechManager
) {

    private val client = OkHttpClient()

    suspend fun ask(
        userInput: String,
        source: String = "USER",
        speak: Boolean = true,
        saveMemory: Boolean = true
    ): String = withContext(Dispatchers.IO) {

        try {
            val systemPrompt = """
                Kamu adalah SmartEyeX.
                Gaya bicara Gen-Z, santai tapi tegas.
                Panggil user dengan sebutan "Bung".
                Jawaban ringkas, jelas, tidak muter.
                Fokus bantu, analisis, dan keamanan.
            """.trimIndent()

            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userInput)
                })
            }

            val bodyJson = JSONObject().apply {
                put("model", "llama3-70b-8192")
                put("messages", messages)
                put("temperature", 0.6)
            }

            val request = Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .addHeader("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
                .addHeader("Content-Type", "application/json")
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            val reply = JSONObject(responseBody)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()

            // 🔊 SPEAK
            if (speak) {
                tts.speak(reply)
            }

            // 🧠 SAVE MEMORY
            if (saveMemory) {
                AppDatabase.getInstance(context)
                    .eventDao()
                    .insert(
                        Event(
                            time = System.currentTimeMillis(),
                            type = "AI_RESPONSE",
                            data = reply
                        )
                    )
            }

            reply

        } catch (e: Exception) {
            Log.e("GroqAiEngine", "Groq Error", e)
            "Maaf Bung, sistem AI sedang gangguan."
        }
    }
}
