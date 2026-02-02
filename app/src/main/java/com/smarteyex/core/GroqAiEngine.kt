package com.smarteyex.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GroqAiEngine {

    private const val BASE_URL = "https://api.groq.com/openai/v1/chat/completions"
    private const val MODEL = "llama3-70b-8192"

    // API KEY DIAMBIL DARI ENV (GitHub Secret / local env)
    private val apiKey: String =
        System.getenv("GROQ_API_KEY")
            ?: throw IllegalStateException("GROQ_API_KEY not set")

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * MAIN CHAT ENTRY
     */
    suspend fun chat(
        userInput: String,
        topic: String?,
        emotion: AppState.Emotion
    ): String = withContext(Dispatchers.IO) {

        try {
            val payload = buildPayload(userInput, topic, emotion)
            val request = Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    payload.toString()
                ))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext fallback(userInput)
                }

                val body = response.body?.string() ?: return@withContext fallback(userInput)
                parseResponse(body)
            }

        } catch (e: IOException) {
            fallback(userInput)
        } catch (e: Exception) {
            fallback(userInput)
        }
    }

    /**
     * BUILD PROMPT SESUAI VISI GEN-Z MANUSIAWI
     */
    private fun buildPayload(
        input: String,
        topic: String?,
        emotion: AppState.Emotion
    ): JSONObject {

        val systemPrompt = """
            Lu adalah AI pendamping manusia bernama Smart.
            Cara ngomong lu:
            - Gen-Z
            - Santai
            - Kadang nyeleneh
            - Tetep sopan
            - Punya emosi, tapi rasional
            - Jangan bahasa baku
            - Jangan sok pinter
            
            Emosi lu sekarang: ${emotion.name}
            Topik aktif: ${topic ?: "bebas"}
            
            Jangan kepanjangan.
            Jawab kayak manusia beneran.
        """.trimIndent()

        val messages = listOf(
            JSONObject()
                .put("role", "system")
                .put("content", systemPrompt),
            JSONObject()
                .put("role", "user")
                .put("content", input)
        )

        return JSONObject()
            .put("model", MODEL)
            .put("temperature", 0.8)
            .put("messages", messages)
    }

    /**
     * PARSE JAWABAN
     */
    private fun parseResponse(raw: String): String {
        val json = JSONObject(raw)
        val choices = json.getJSONArray("choices")
        if (choices.length() == 0) return fallback("")

        return choices
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }

    /**
     * FALLBACK KALO API MATI
     */
    private fun fallback(input: String): String {
        return when {
            input.contains("capek", true) ->
                "Iya sih… lu kelihatan capek. Rehat bentar kek."
            input.contains("kesel", true) ->
                "Santai, tarik napas dulu. Jangan meledak."
            else ->
                "Hmm… gue nangkepnya sih gitu. Lanjut aja."
        }
    }
}