package com.smarteyex.core.wa

import android.os.Handler
import android.os.Looper
import com.smarteyex.core.VoiceService
import com.smarteyex.core.AppSpeak

object DelayManager {

    fun set(text:String){
        val minutes = Regex("\\d+").find(text)?.value?.toInt() ?: return

        Handler(Looper.getMainLooper()).postDelayed({
            AppSpeak.speak("Waktu tunggu $minutes menit selesai")
        }, minutes*60*1000L)
    }
}