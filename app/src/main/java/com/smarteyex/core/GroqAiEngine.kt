package com.smarteyex.core.ai

import android.content.Context
import com.smarteyex.core.memory.MemoryManager
import com.smarteyex.core.voice.VoiceEngine
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import com.smarteyex.core.BuildConfig

class GroqAiEngine(private val context: Context) {

    private val client = OkHttpClient()
    private val memory = MemoryManager(context)
    private val voice = VoiceEngine(context)
private val API_KEY = BuildConfig.GROQ_API_KEY
    // GANTI DENGAN API KEY LU
    val request = Request.Builder()
    .url(ENDPOINT)
    .addHeader("Authorization", "Bearer $API_KEY")
    .post(body)
    .build()
    private val ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"

    fun ask(userText: String) {

        memory.save("user_input", userText)

        val payload = JSONObject().apply {
            put("model", "llama3-70b-8192")
            put("messages", listOf(
                JSONObject().put("role", "system")
                    .put("content", "Kamu adalah SmartEyeX, AI asisten futuristik, singkat, tegas."),
                JSONObject().put("role", "user")
                    .put("content", userText)
            ))
        }

        val body = RequestBody.create(
            MediaType.parse("application/json"),
            payload.toString()
        )

        val request = Request.Builder()
            .url(ENDPOINT)
            .addHeader("Authorization", "Bearer $API_KEY")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                voice.speak("Koneksi AI gagal")
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body()?.string() ?: return
                val json = JSONObject(result)
                val answer = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                memory.save("ai_response", answer)
                voice.speak(answer)
            }
        })
    }
}