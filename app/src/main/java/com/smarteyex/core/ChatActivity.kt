package com.smarteyex.core.chat

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.smarteyex.core.R
import com.smarteyex.core.SmartEyeXApp
import com.smarteyex.core.ai.GroqAiEngine
import com.smarteyex.core.state.AppState
import com.smarteyex.core.voice.VoiceService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var appState: AppState
    private lateinit var aiEngine: GroqAiEngine
    private lateinit var voiceService: VoiceService

    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val app = application as SmartEyeXApp
        appState = app.appState
        aiEngine = app.aiEngine
        voiceService = app.voiceService

        setupChat()
        observeEmotion()
        setupInput()
    }

    /**
     * ===============================
     * CHAT CORE
     * ===============================
     */
    private fun setupChat() {
        chatAdapter = ChatAdapter()

        val recycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvChat)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = chatAdapter
    }

    /**
     * ===============================
     * INPUT USER
     * ===============================
     * User = pusat obrolan
     */
    private fun setupInput() {
        val input = findViewById<android.widget.EditText>(R.id.etMessage)
        val send = findViewById<View>(R.id.btnSend)

        send.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            input.setText("")
            onUserMessage(text)
        }
    }

    /**
     * ===============================
     * USER MESSAGE FLOW
     * ===============================
     */
    private fun onUserMessage(text: String) {
        chatAdapter.addUserMessage(text)

        lifecycleScope.launch {
            // AI mikir dulu, ga nyerobot
            delay(calculateHumanDelay())

            if (!shouldAiRespond(text)) return@launch

            val response = aiEngine.generateResponse(
                userText = text,
                emotion = appState.currentEmotion,
                mode = appState.currentMode
            )

            chatAdapter.addAiMessage(response.text)

            if (response.shouldSpeak) {
                voiceService.speak(
                    text = response.text,
                    emotion = appState.currentEmotion
                )
            }
        }
    }

    /**
     * ===============================
     * EMOTION OBSERVER
     * ===============================
     * Chat adaptif ke kondisi user
     */
    private fun observeEmotion() {
        lifecycleScope.launch {
            appState.emotionFlow.collect { emotion ->
                chatAdapter.updateEmotionContext(emotion)
            }
        }
    }

    /**
     * ===============================
     * AI SELF RESTRAINT
     * ===============================
     */
    private fun shouldAiRespond(text: String): Boolean {
        // AI ga harus selalu jawab
        if (appState.isConversationCrowded) return false
        if (appState.currentEmotion.isOverwhelmed()) return false

        // AI nimbrung kalau relevan
        return true
    }

    /**
     * ===============================
     * HUMAN TIMING
     * ===============================
     */
    private fun calculateHumanDelay(): Long {
        return (300L..700L).random()
    }
}