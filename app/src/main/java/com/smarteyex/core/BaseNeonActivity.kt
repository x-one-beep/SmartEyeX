package com.smarteyex.core.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smarteyex.core.state.AppState
import kotlinx.coroutines.launch

abstract class BaseNeonActivity : AppCompatActivity() {

    protected lateinit var touchLayer: NeonTouchLayer

    // setiap Activity WAJIB nentuin ini
    abstract fun neonMode(): NeonTouchLayer.Mode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // layer efek sentuh
        touchLayer = NeonTouchLayer(this).apply {
            setMode(neonMode())
        }

        observeAppState()
    }

    protected fun attachTouchLayer() {
        addContentView(
            touchLayer,
            touchLayer.layoutParams
        )
    }

    private fun observeAppState() {
        lifecycleScope.launch {
            AppState.isAwake.collect { awake ->
                touchLayer.setActive(awake)
            }
        }

        lifecycleScope.launch {
            AppState.appMode.collect { mode ->
                touchLayer.setLowPower(
                    mode == AppState.AppMode.SCHOOL
                )
            }
        }
    }
}