package com.smarteyex.core

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.smarteyex.core.R

class MainActivity : AppCompatActivity() {

    private val REQ_PERMS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainfull)

        AppContextHolder.context = applicationContext
        requestRuntimePermissions()
    }

    private fun requestRuntimePermissions() {
        val perms = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )

        val need = perms.filter { perm ->
            ContextCompat.checkSelfPermission(
                this@MainActivity,
                perm
            ) != PackageManager.PERMISSION_GRANTED
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

    @Deprecated("Deprecated in API 33+, still required for backward compatibility")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
        continueBoot()
    }

    private fun continueBoot() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

        SpeechOutput.init(this)
        SensorBrainIntegrator.init(this)
        SmartDashboard.init(this)
    }
}