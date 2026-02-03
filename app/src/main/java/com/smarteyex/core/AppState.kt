package com.smarteyex.core.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

class AppState {

    enum class Mode { NORMAL, SCHOOL, GAME, OFF }
    enum class Emotion { CAPEK, SENANG, SEDIH, MARAH, KOSONG
        fun isOverwhelmed() = this == MARAH || this == SEDIH
    }

    data class Context(
        val timeOfDay: TimeOfDay,
        val locationRough: String? = null,
        val lastInteractionMs: Long = System.currentTimeMillis()
    )

    enum class TimeOfDay { PAGI, SIANG, SORE, MALAM }

    private val _mode = MutableStateFlow(Mode.NORMAL)
    private val _emotion = MutableStateFlow(Emotion.KOSONG)
    private val _context = MutableStateFlow(Context(TimeOfDay.PAGI))
    private val _isMicAllowed = MutableStateFlow(true)
    private val _isBatteryLow = MutableStateFlow(false)
    private val _isUserBusy = MutableStateFlow(false)
    private val _conversationCrowded = AtomicBoolean(false)

    val stateFlow: StateFlow<AppStateSnapshot> = MutableStateFlow(AppStateSnapshot()).asStateFlow()
    val currentMode get() = _mode.value
    val currentEmotion get() = _emotion.value
    val currentContext get() = _context.value
    val isBatteryLow get() = _isBatteryLow.value
    val isUserBusy get() = _isUserBusy.value
    val isConversationCrowded get() = _conversationCrowded.get()

    fun isMicAllowed() = _isMicAllowed.value && currentMode != Mode.OFF
    fun setMicAllowed(allowed: Boolean) { _isMicAllowed.value = allowed }
    fun setMode(mode: Mode) { _mode.value = mode }
    fun setBatteryLow(low: Boolean) { _isBatteryLow.value = low }
    fun setUserBusy(busy: Boolean) { _isUserBusy.value = busy }
    fun setConversationCrowded(crowded: Boolean) { _conversationCrowded.set(crowded) }

    fun updateFromSpeech(speech: String) {
        val lower = speech.lowercase()
        val newEmotion = when {
            listOf("capek", "lelah", "pusing").any { lower.contains(it) } -> Emotion.CAPEK
            listOf("seneng", "happy", "mantap").any { lower.contains(it) } -> Emotion.SENANG
            listOf("sedih", "kecewa").any { lower.contains(it) } -> Emotion.SEDIH
            listOf("marah", "kesel").any { lower.contains(it) } -> Emotion.MARAH
            else -> Emotion.KOSONG
        }
        if (newEmotion != _emotion.value) _emotion.value = newEmotion
        touchInteraction()
    }

    private fun touchInteraction() {
        _context.value = _context.value.copy(lastInteractionMs = System.currentTimeMillis())
    }

    fun updateTime(hour: Int) {
        val time = when (hour) {
            in 5..10 -> TimeOfDay.PAGI
            in 11..14 -> TimeOfDay.SIANG
            in 15..18 -> TimeOfDay.SORE
            else -> TimeOfDay.MALAM
        }
        _context.value = _context.value.copy(timeOfDay = time)
    }

    data class AppStateSnapshot(
        val mode: Mode = Mode.NORMAL,
        val emotion: Emotion = Emotion.KOSONG,
        val context: Context = Context(TimeOfDay.PAGI)
    )
}