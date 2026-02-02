package com.smarteyex.core.chat

import android.os.Bundle
import android.os.Handler
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.smarteyex.core.R
import com.smarteyex.core.state.AppState
import com.smarteyex.core.voice.VoiceEngine
import com.smarteyex.core.voice.VoiceService
import com.smarteyex.core.ai.GroqAiEngine
import com.smarteyex.core.BaseNeonActivity
import com.smarteyex.core.NeonTouchLayer

class ChatActivity : BaseNeonActivity() {

    private lateinit var chatContainer: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var inputText: EditText
    private lateinit var sendButton: ImageButton
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Inject touch effects layer (gelombang neon)
        val rootLayout = findViewById<LinearLayout>(R.id.chatRoot)
        val touchLayer = NeonTouchLayer(this, NeonTouchLayer.EffectType.CHAT_WAVE)
        rootLayout.addView(touchLayer)

        // Bind UI
        chatContainer = findViewById(R.id.chatContainer)
        scrollView = findViewById(R.id.chatScroll)
        inputText = findViewById(R.id.chatInput)
        sendButton = findViewById(R.id.chatSend)

        // Initialize Voice & AI
        VoiceService.init(this)
        VoiceEngine.init(this)
        GroqAiEngine.init(this)

        initSendButton()
        initVoiceListener()
        initAIBackgroundListener()
        applyUITheme()
    }

    private fun initSendButton() {
        sendButton.setOnClickListener {
            val userMessage = inputText.text.toString()
            if (userMessage.isNotEmpty()) {
                addChatBubble(userMessage, true)
                inputText.text.clear()
                sendToAI(userMessage)
            }
        }
    }

    private fun addChatBubble(message: String, isUser: Boolean) {
        val bubble = TextView(this).apply {
            text = message
            textSize = 16f
            setPadding(20, 12, 20, 12)
            setBackgroundResource(
                if (isUser) R.drawable.chat_user_bubble else R.drawable.chat_ai_bubble
            )
            setTextColor(android.graphics.Color.WHITE)
        }
        chatContainer.addView(bubble)
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun sendToAI(message: String) {
        handler.postDelayed({
            // Panggil Groq AI engine
            val aiResponse = GroqAiEngine.getResponse(message, AppState.contextMemory)
            addChatBubble(aiResponse, false)
            // TTS dengan emosi AI & mode
            VoiceService.speak(aiResponse, AppState.currentEmotion, AppState.isSchoolMode)
        }, 500)
    }

    private fun initVoiceListener() {
        VoiceEngine.startListening { recognizedText ->
            if (recognizedText.isNotEmpty()) {
                addChatBubble(recognizedText, true)
                sendToAI(recognizedText)
            }
        }
    }

    private fun initAIBackgroundListener() {
        // Always-listening background AI
        GroqAiEngine.startBackgroundContext { message, priority ->
            // Misal ada notif WA atau user chat
            handler.post {
                addChatBubble(message, false)
                VoiceService.speak(message, AppState.currentEmotion, AppState.isSchoolMode)
            }
        }
    }

    private fun applyUITheme() {
        // Neon / gelombang animasi sesuai style ChatActivity
        // Bisa ditambah glow / gradient background dari BaseNeonActivity
    }
}