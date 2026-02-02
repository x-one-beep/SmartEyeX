package com.smarteyex.core

object NavigationStateManager {

    enum class State {
        IDLE,
        LISTENING,
        SPEAKING,
        WAITING_USER_RESPONSE,
        PROCESSING_REPLY
    }

    @Volatile
    private var currentState: State = State.IDLE

    fun setState(state: State) {
        currentState = state
    }

    fun getState(): State = currentState

    fun isBusy(): Boolean {
        return currentState == State.SPEAKING || currentState == State.LISTENING
    }
}