package com.smarteyex.core.ai

import android.content.Context
import com.smarteyex.app.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class GroqAiEngine(context: Context) {

    private val client = OkHttpClient()
    private val API_KEY = BuildConfig.GROQ_API_KEY
    private val ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"

    fun ask(
        userText: String,
        onResult: (String) -> Unit,
        onError: () -> Unit
    ) {
        val payload = JSONObject().apply {
            put("model", "llama3-70b-8192")
            put("messages", listOf(
                JSONObject().put("role","system")
                    .put("content","Kamu SmartEyeX, singkat, tegas."),
                JSONObject().put("role","user")
                    .put("content",userText)
            ))
        }

        val body = payload.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(ENDPOINT)
            .addHeader("Authorization","Bearer $API_KEY")
            .post(body)
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError()
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string() ?: return)
                val answer = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                onResult(answer)
            }
        })
    }
}