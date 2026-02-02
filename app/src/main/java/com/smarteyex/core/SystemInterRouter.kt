package com.smarteyex.core

import android.content.Context
import android.os.BatteryManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object SystemIntentRouter {

    /**
     * RETURN true kalau intent ketangani
     */
    fun handle(input: String): Boolean {
        val text = input.lowercase()

        return when {
            text.contains("jam berapa") ||
            text.contains("pukul berapa") -> {
                speakTime()
                true
            }

            text.contains("tanggal") ||
            text.contains("hari apa") -> {
                speakDate()
                true
            }

            text.contains("baterai") ||
            text.contains("batre") -> {
                speakBattery()
                true
            }

            text.contains("panas") ||
            text.contains("dingin") ||
            text.contains("cuaca") -> {
                WeatherProvider.speakWeather()
                true
            }

            else -> false
        }
    }

    private fun speakTime() {
        val time = LocalTime.now()
            .format(DateTimeFormatter.ofPattern("HH:mm"))
        AppSpeak.say("Sekarang jam $time.")
    }

    private fun speakDate() {
        val date = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        AppSpeak.say("Hari ini tanggal $date.")
    }

    private fun speakBattery() {
        val context = AppContext.app
        val manager =
            context.getSystemService(Context.BATTERY_SERVICE)
                    as BatteryManager

        val level =
            manager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            )

        AppSpeak.say("Baterai lu sisa $level persen.")
    }
}