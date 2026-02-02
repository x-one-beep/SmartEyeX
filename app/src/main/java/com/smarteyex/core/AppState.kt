package com.smarteyex.core

object AppState {

    /**
     * MODE PENGGUNA
     */
    enum class UserMode {
        NORMAL,
        SEKOLAH,
        GAME
    }

    /**
     * MODE AI (ENERGI & PERILAKU)
     */
    enum class AiMode {
        ACTIVE,          // full ngobrol
        PASSIVE_AWARE,   // denger tapi diem
        SLEEP            // hemat daya ekstrem
    }

    /**
     * EMOSI AI (NGARUH KE CARA NGOMONG)
     */
    enum class Emotion {
        NETRAL,
        SENENG,
        SEDIH,
        KESEL,
        CAPEK
    }

    /**
     * STATE GLOBAL
     */
    @Volatile
    var userMode: UserMode = UserMode.NORMAL

    @Volatile
    var aiMode: AiMode = AiMode.ACTIVE

    @Volatile
    var emotion: Emotion = Emotion.NETRAL

    /**
     * KONTROL CAPEK AI (BIAR MANUSIAWI)
     */
    private var interactionCount = 0
    private const val FATIGUE_THRESHOLD = 15

    /**
     * Dipanggil tiap AI interaksi
     */
    fun registerInteraction() {
        interactionCount++

        if (interactionCount >= FATIGUE_THRESHOLD) {
            emotion = Emotion.CAPEK
            aiMode = AiMode.PASSIVE_AWARE
        }
    }

    /**
     * RESET KALO UDAH TENANG
     */
    fun recover() {
        interactionCount = 0
        emotion = Emotion.NETRAL
        aiMode = AiMode.ACTIVE
    }

    /**
     * Dipanggil pas user manggil AI
     */
    fun wakeUp() {
        aiMode = AiMode.ACTIVE
        if (emotion == Emotion.CAPEK) {
            emotion = Emotion.NETRAL
        }
    }

    /**
     * Dipanggil kalo user lagi sibuk / layar mati lama
     */
    fun goPassive() {
        aiMode = AiMode.PASSIVE_AWARE
    }

    /**
     * Deep sleep (hemat daya ekstrem)
     */
    fun sleep() {
        aiMode = AiMode.SLEEP
    }
}
[1/2, 22.34] owner muda: package com.smarteyex.core

object ModeManager {

    fun setSchoolMode() {
        AppState.userMode = AppState.UserMode.SEKOLAH
        AppState.aiMode = AppState.AiMode.PASSIVE_AWARE
        AppState.emotion = AppState.Emotion.NETRAL

        AppSpeak.say(
            "Mode sekolah aktif. Gue diem, kecuali lu butuh."
        )
    }

    fun setGameMode() {
        AppState.userMode = AppState.UserMode.GAME
        AppState.aiMode = AppState.AiMode.PASSIVE_AWARE
        AppState.emotion = AppState.Emotion.SENENG

        AppSpeak.say(
            "Mode game nyala. Fokus main, gue standby."
        )
    }

    fun setNormalMode() {
        AppState.userMode = AppState.UserMode.NORMAL
        AppState.aiMode = AppState.AiMode.ACTIVE
        AppState.emotion = AppState.Emotion.NETRAL

        AppSpeak.say(
            "Balik normal. Gas ngobrol lagi."
        )
    }

    /**
     * DETEKSI MODE DARI UCAPAN TANPA KATA KAKU
     */
    fun detectAndApply(input: String): Boolean {
        val text = input.lowercase()

        return when {
            text.contains("mode sekolah") ||
            text.contains("lagi sekolah") -> {
                setSchoolMode(); true
            }

            text.contains("mode game") ||
            text.contains("lagi main") -> {
                setGameMode(); true
            }

            text.contains("balik normal") ||
            text.contains("mode biasa") -> {
                setNormalMode(); true
            }

            else -> false
        }
    }
}