package com.smarteyex.core

import android.content.Context

object AppSpeak {

    private var engine: VoiceEngine? = null

    fun init(context: Context, onResult: (String) -> Unit) {
        if (engine == null) {
            engine = VoiceEngine(context, onResult)
        }
    }

    fun say(text: String) {
        if (NavigationStateManager.isBusy()) return
        NavigationStateManager.setState(
            NavigationStateManager.State.SPEAKING
        )
        engine?.speak(text)
        NavigationStateManager.setState(
            NavigationStateManager.State.IDLE
        )
    }

    fun listen() {
        if (AppState.userMode == AppState.UserMode.GAME &&
            AppState.aiMode != AppState.AiMode.ACTIVE
        ) return

        NavigationStateManager.setState(
            NavigationStateManager.State.LISTENING
        )
        engine?.listen()
    }

    fun destroy() {
        engine?.shutdown()
        engine = null
    }
}