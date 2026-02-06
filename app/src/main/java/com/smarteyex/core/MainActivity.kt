package com.smarteyex.core

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.smarteyex.fullcore.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_mainfull)

        // global context
        AppContextHolder.context = applicationContext

        // init engine TANPA ubah kode engine
        SpeechOutput.init(this)
        SensorBrainIntegrator.init(this)
        SmartDashboard.init(this)
    }
}