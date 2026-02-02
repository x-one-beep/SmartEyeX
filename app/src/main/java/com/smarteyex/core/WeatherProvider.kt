package com.smarteyex.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WeatherProvider {

    // Pakai Open-Meteo (gratis, tanpa API key)
    private const val BASE_URL =
        "https://api.open-meteo.com/v1/forecast"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun speakWeather() {
        val location = LocationProvider.getLastKnown()
            ?: run {
                AppSpeak.say("Gue belum tau lokasi lu.")
                return
            }

        val url =
            "$BASE_URL?latitude=${location.lat}" +
            "&longitude=${location.lon}" +
            "&current_weather=true"

        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val body = response.body?.string() ?: return
            val json = JSONObject(body)
            val weather =
                json.getJSONObject("current_weather")

            val temp = weather.getDouble("temperature")
            val wind = weather.getDouble("windspeed")

            AppSpeak.say(
                "Sekarang sekitar $temp derajat. " +
                "Anginnya $wind kilo. " +
                "Lumayan kerasa."
            )

        } catch (e: Exception) {
            AppSpeak.say("Cuacanya nggak kebaca sekarang.")
        }
    }
}