package com.smarteyex.core.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AppState {

    // === MODE APLIKASI ===
    enum class AppMode {
        NORMAL,
        SCHOOL,
        GAME
    }

    // === EMOSI AI ===
    enum class Emotion {
        CALM,
        HAPPY,
        TIRED,
        SAD,
        ANGRY,
        EMPTY
    }

    // === STATE UTAMA ===
    private val _appMode = MutableStateFlow(AppMode.NORMAL)
    val appMode: StateFlow<AppMode> = _appMode

    private val _emotion = MutableStateFlow(Emotion.CALM)
    val emotion: StateFlow<Emotion> = _emotion

    private val _isListening = MutableStateFlow(true)
    val isListening: StateFlow<Boolean> = _isListening

    private val _isAwake = MutableStateFlow(true)
    val isAwake: StateFlow<Boolean> = _isAwake

    // === UPDATE METHODS (SATU PINTU) ===
    fun setMode(mode: AppMode) {
        _appMode.value = mode
    }

    fun setEmotion(emotion: Emotion) {
        _emotion.value = emotion
    }

    fun setListening(active: Boolean) {
        _isListening.value = active
    }

    fun setAwake(awake: Boolean) {
        _isAwake.value = awake
    }
}