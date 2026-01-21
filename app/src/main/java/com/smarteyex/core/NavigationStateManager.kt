package com.smarteyex.core

object NavigationStateManager {

    enum class Screen {
        DASHBOARD,
        CAMERA,
        SETTINGS
    }

    private var current = Screen.DASHBOARD

    fun set(screen: Screen) {
        current = screen
    }

    fun get(): Screen = current
}
