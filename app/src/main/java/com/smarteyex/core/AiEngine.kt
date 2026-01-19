package com.smarteyex.core

import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class GroqAiEngine {

    private val client = OkHttpClient()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun ask(
        userPrompt: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                val json = JSONObject().apply {
                    put("model", "llama3-70b-8192")
                    put("messages", listOf(
                        JSONObject().apply {
                            put("role", "user")
                            put("content", userPrompt)
                        }
                    ))
                }

                val body = RequestBody.create(
                    "application/json".toMediaType(),
                    json.toString()
                )

                val request = Request.Builder()
                    .url("https://api.groq.com/openai/v1/chat/completions")
                    .addHeader("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
                    .post(body)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        onError(e.message ?: "Network error")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.body?.string()?.let { res ->
                            val text = JSONObject(res)
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")

                            onResult(text)
                        } ?: onError("Empty response")
                    }
                })

            } catch (e: Exception) {
                onError(e.message ?: "AI error")
            }
        }
    }

    fun destroy() {
        scope.cancel()
    }
}
