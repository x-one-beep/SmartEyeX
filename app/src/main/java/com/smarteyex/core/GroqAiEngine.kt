package com.smarteyex.core

import android.content.Context
import okhttp3.*
import org.json.JSONObject

class GroqAiEngine(private val context: Context) {

    private val client = OkHttpClient()

    fun ask(prompt: String, callback: (String) -> Unit) {

        val body = """
        {
          "model": "llama3-70b-8192",
          "messages": [
            {"role":"user","content":"$prompt"}
          ]
        }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer YOUR_SECRET_API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(MediaType.get("application/json"), body))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                callback("Maaf Bung, koneksi bermasalah.")
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val json = JSONObject(response.body()?.string() ?: "")
                    val answer = json
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    callback(answer)
                } catch (e: Exception) {
                    callback("Jawaban AI gagal diproses.")
                }
            }
        })
    }
}