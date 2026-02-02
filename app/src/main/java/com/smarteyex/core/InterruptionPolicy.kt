package com.smarteyex.core

object InterruptionPolicy {

    fun canInterrupt(): Boolean {
        if (AppState.userMode == AppState.UserMode.SEKOLAH) return false
        if (NavigationStateManager.isBusy()) return false
        if (AppState.aiMode == AppState.AiMode.PASSIVE_AWARE) return false
        return true
    }

    fun notifyWhenSafe(message: String) {
        if (canInterrupt()) {
            AppSpeak.say(message)
        } else {
            // tahan sampai aman
            SafeNotifier.enqueue(message)
        }
    }
}