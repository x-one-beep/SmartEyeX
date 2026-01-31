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
    val apiKey = BuildConfig.GROQ_API_KEY
    if (apiKey.isBlank()) {
        onError()
        return
    }

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
        .addHeader("Authorization", "Bearer $apiKey")
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            onError()
        }

        override fun onResponse(call: Call, response: Response) {
            try {
                if (!response.isSuccessful) {
                    onError()
                    return
                }
                val resStr = response.body?.string()
                if (resStr.isNullOrEmpty()) {
                    onError()
                    return
                }

                val jsonRes = JSONObject(resStr)
                val choices = jsonRes.optJSONArray("choices")
                val content = choices?.optJSONObject(0)
                    ?.optJSONObject("message")
                    ?.optString("content")

                if (content.isNullOrBlank()) {
                    onError()
                    return
                }

                onResult(content)

            } catch (e: Exception) {
                onError()
            }
        }
    })
}