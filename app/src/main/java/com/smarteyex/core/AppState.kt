package com.smarteyex.core.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * =================================================
 * APP STATE — SINGLE SOURCE OF TRUTH
 * =================================================
 * Semua modul WAJIB baca dari sini.
 * Tidak ada logic emosi / mode / mic di tempat lain.
 */
class AppState {

    // ===============================
    // MODE & STATUS
    // ===============================
    enum class Mode { NORMAL, SCHOOL, GAME, OFF }

    enum class Emotion {
        CAPEK, SENANG, SEDIH, MARAH, KOSONG;

        fun isOverwhelmed(): Boolean =
            this == MARAH || this == SEDIH
    }

    data class Context(
        val timeOfDay: TimeOfDay,
        val locationRough: String? = null,
        val lastInteractionMs: Long = System.currentTimeMillis()
    )

    enum class TimeOfDay { PAGI, SIANG, SORE, MALAM }

    // ===============================
    // INTERNAL STATE
    // ===============================
    private val _mode = MutableStateFlow(Mode.NORMAL)
    private val _emotion = MutableStateFlow(Emotion.KOSONG)
    private val _context = MutableStateFlow(
        Context(timeOfDay = TimeOfDay.PAGI)
    )

    private val _isMicAllowed = MutableStateFlow(true)
    private val _isBatteryLow = MutableStateFlow(false)
    private val _isUserBusy = MutableStateFlow(false)

    // crowd / social detection
    private val _conversationCrowded = AtomicBoolean(false)

    // ===============================
    // PUBLIC FLOWS
    // ===============================
    val modeFlow: StateFlow<Mode> = _mode.asStateFlow()
    val emotionFlow: StateFlow<Emotion> = _emotion.asStateFlow()
    val contextFlow: StateFlow<Context> = _context.asStateFlow()

    // ===============================
    // QUICK ACCESS (READ ONLY)
    // ===============================
    val currentMode: Mode get() = _mode.value
    val currentEmotion: Emotion get() = _emotion.value
    val currentContext: Context get() = _context.value
    val isBatteryLow: Boolean get() = _isBatteryLow.value
    val isUserBusy: Boolean get() = _isUserBusy.value
    val isConversationCrowded: Boolean get() = _conversationCrowded.get()

    // ===============================
    // MIC & PRIVACY
    // ===============================
    fun isMicAllowed(): Boolean {
        return _isMicAllowed.value && currentMode != Mode.OFF
    }

    fun setMicAllowed(allowed: Boolean) {
        _isMicAllowed.value = allowed
    }

    // ===============================
    // MODE CONTROL
    // ===============================
    fun setMode(mode: Mode) {
        _mode.value = mode
    }

    // ===============================
    // EMOTION ENGINE (LIGHTWEIGHT)
    // ===============================
    fun updateFromSpeech(speech: String) {
        val lower = speech.lowercase()

        val newEmotion = when {
            listOf("capek", "lelah", "pusing").any { lower.contains(it) } ->
                Emotion.CAPEK
            listOf("seneng", "happy", "mantap").any { lower.contains(it) } ->
                Emotion.SENANG
            listOf("sedih", "kecewa").any { lower.contains(it) } ->
                Emotion.SEDIH
            listOf("marah", "kesel").any { lower.contains(it) } ->
                Emotion.MARAH
            else -> Emotion.KOSONG
        }

        if (newEmotion != _emotion.value) {
            _emotion.value = newEmotion
        }

        touchInteraction()
    }

    // ===============================
    // CONTEXT UPDATE
    // ===============================
    fun updateTime(hour: Int) {
        val time = when (hour) {
            in 5..10 -> TimeOfDay.PAGI
            in 11..14 -> TimeOfDay.SIANG
            in 15..18 -> TimeOfDay.SORE
            else -> TimeOfDay.MALAM
        }
        _context.value = _context.value.copy(timeOfDay = time)
    }

    fun setUserBusy(busy: Boolean) {
        _isUserBusy.value = busy
    }

    fun setBatteryLow(low: Boolean) {
        _isBatteryLow.value = low
    }

    fun setConversationCrowded(crowded: Boolean) {
        _conversationCrowded.set(crowded)
    }

    // ===============================
    // INTERACTION TRACKING
    // ===============================
    private fun touchInteraction() {
        _context.value = _context.value.copy(
            lastInteractionMs = System.currentTimeMillis()
        )
    }
}