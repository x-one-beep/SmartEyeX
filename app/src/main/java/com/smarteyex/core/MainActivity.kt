package com.smarteyex.core

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val REQ_PERMS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI boleh minimal, ga pengaruh otak
        setContentView(R.layout.activity_mainfull)

        // Context global WAJIB
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

    @Deprecated("Still required for < API 33")
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

        // MAU user allow / deny → app TETAP JALAN
        continueBoot()
    }

    private fun continueBoot() {

        // ⛔ JANGAN DI-BLOCK, biar user tau
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

        // 🫀 NYALAIN JANTUNG (FOREGROUND SERVICE)
        val brainIntent = Intent(this, BrainForegroundService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(brainIntent)
        } else {
            startService(brainIntent)
        }

        // ❗ Activity BOLEH MATI
        finish()
    }
}