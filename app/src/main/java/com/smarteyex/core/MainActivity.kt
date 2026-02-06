package com.smarteyex.core

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val REQ_PERMS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI gate (boleh layout lo sendiri)
        setContentView(R.layout.activity_main)

        // Global context
        AppContextHolder.context = applicationContext

        requestRuntimePermissions()
    }

    private fun requestRuntimePermissions() {
        val perms = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )

        val need = perms.filter {
            ContextCompat.checkSelfPermission(this, it)
                    != PackageManager.PERMISSION_GRANTED
        }

        if (need.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                need.toTypedArray(),
                REQ_PERMS
            )
        } else {
            continueBoot()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        continueBoot()
    }

    private fun continueBoot() {
        // arahkan user manual (WAJIB)
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

        // INIT ENGINE (TANPA UBAH KODE)
        SpeechOutput.init(this)
        SensorBrainIntegrator.init(this)
        SmartDashboard.init(this)

        // jangan finish
    }
}