package com.smarteyex.core

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class GroqAiEngine {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.groq.com/")  // Ganti dengan endpoint Groq
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(GroqApi::class.java)

    // Fungsi untuk chat interaktif
    fun chatWithAI(message: String, callback: (String) -> Unit) {
        val request = GroqRequest(message = message, apiKey = BuildConfig.GROQ_API_KEY)  // Dari GitHub Secret
        api.chat(request).enqueue(object : Callback<GroqResponse> {
            override fun onResponse(call: Call<GroqResponse>, response: Response<GroqResponse>) {
                callback(response.body()?.response ?: "Error")
            }
            override fun onFailure(call: Call<GroqResponse>, t: Throwable) {
                callback("Network error")
            }
        })
    }

    // Fungsi untuk generate balasan random
    fun generateRandomResponse(callback: (String) -> Unit) {
        chatWithAI("Generate a random fun response", callback)
    }
}

interface GroqApi {
    @POST("chat")  // Endpoint placeholder
    fun chat(@Body request: GroqRequest): Call<GroqResponse>
}

data class GroqRequest(val message: String, val apiKey: String)
data class GroqResponse(val response: String)