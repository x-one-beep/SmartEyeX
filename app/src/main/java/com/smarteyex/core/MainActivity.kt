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
        val btnStart=findViewById<Button>(R.id.btnStart)
       val btnstart=findViewById<TextView>(R.id.tvStatus)
       tvStatus.translationY = 50f
tvStatus.alpha = 0f

tvStatus.animate()
    .translationY(0f)
    .alpha(1f)
    .setDuration(900)
    .start()
    
        btnStart.scaleX = 0.8f
btnStart.scaleY = 0.8f
btnStart.alpha = 0f
btnStart.visibility = View.VISIBLE

btnStart.animate()
    .scaleX(1f)
    .scaleY(1f)
    .alpha(1f)
    .setStartDelay(1200)
    .setDuration(600)
    .start()

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
