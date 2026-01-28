package com.smarteyex.core.ai

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import com.smarteyex.app.BuildConfig

class GroqAiEngine(private val context: Context) {

    private val client = OkHttpClient()

    fun ask(
        prompt: String,
        onResult: (String) -> Unit,
        onError: () -> Unit
    ) {
        val json = JSONObject().apply {
            put("model", "llama3-8b-8192")
            put("messages", listOf(
                mapOf("role" to "user", "content" to prompt)
            ))
        }

        val body = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader(
    "Authorization",
    "Bearer ${BuildConfig.GROQ_API_KEY}"
)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                onError()
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val res = response.body?.string() ?: ""
                    val content = JSONObject(res)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    onResult(content)
                } catch (e: Exception) {
                    onError()
                }
            }
        })
    }
}