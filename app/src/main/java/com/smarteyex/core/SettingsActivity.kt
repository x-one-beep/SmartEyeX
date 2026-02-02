package com.smarteyex.core.settings

import android.os.Bundle
import android.widget.Button
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.smarteyex.core.R
import com.smarteyex.core.BaseNeonActivity
import com.smarteyex.core.NeonTouchLayer
import com.smarteyex.core.state.AppState
import com.smarteyex.core.voice.VoiceService

class SettingsActivity : BaseNeonActivity() {

    private lateinit var schoolModeSwitch: Switch
    private lateinit var gameModeSwitch: Switch
    private lateinit var alwaysListenSwitch: Switch
    private lateinit var emotionSeekBar: SeekBar
    private lateinit var aiRestButton: Button
    private lateinit var resetMemoryButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val rootLayout = findViewById<android.widget.LinearLayout>(R.id.settingsRoot)
        val touchLayer = NeonTouchLayer(this, NeonTouchLayer.EffectType.SETTINGS_PANEL)
        rootLayout.addView(touchLayer)

        schoolModeSwitch = findViewById(R.id.switchSchoolMode)
        gameModeSwitch = findViewById(R.id.switchGameMode)
        alwaysListenSwitch = findViewById(R.id.switchAlwaysListen)
        emotionSeekBar = findViewById(R.id.seekBarEmotion)
        aiRestButton = findViewById(R.id.btnAiRest)
        resetMemoryButton = findViewById(R.id.btnResetMemory)

        initControls()
    }

    private fun initControls() {
        schoolModeSwitch.isChecked = AppState.isSchoolMode
        gameModeSwitch.isChecked = AppState.isGameMode
        alwaysListenSwitch.isChecked = AppState.isAlwaysListening
        emotionSeekBar.progress = AppState.currentEmotion

        schoolModeSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            AppState.isSchoolMode = isChecked
            VoiceService.setSchoolMode(isChecked)
        }

        gameModeSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            AppState.isGameMode = isChecked
            VoiceService.setGameMode(isChecked)
        }

        alwaysListenSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            AppState.isAlwaysListening = isChecked
            VoiceService.setAlwaysListening(isChecked)
        }

        emotionSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                AppState.currentEmotion = progress
                VoiceService.setEmotionLevel(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        aiRestButton.setOnClickListener {
            // AI capek, turunkan efek, matiin suara sementara
            AppState.isAIResting = true
            VoiceService.setRestingMode(true)
        }

        resetMemoryButton.setOnClickListener {
            AppState.memoryList.clear()
        }
    }
}