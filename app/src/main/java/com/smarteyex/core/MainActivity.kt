package com.smarteyex.core

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.smarteyex.app.R

class MainActivity : AppCompatActivity() {

    private lateinit var clock: TextView
    private lateinit var date: TextView
    private lateinit var chat: LinearLayout

    private lateinit var ai: GroqAiEngine
    private lateinit var voice: VoiceEngine
    private lateinit var clockManager: ClockManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clock = findViewById(R.id.tvClock)
        date = findViewById(R.id.tvDate)
        chat = findViewById(R.id.chatContainer)

        ai = GroqAiEngine()
        voice = VoiceEngine(this)
        clockManager = ClockManager()

        clockManager.start { t, d ->
            clock.text = t
            date.text = d
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clockManager.stop()
        voice.shutdown()
        ai.destroy()
    }
}
