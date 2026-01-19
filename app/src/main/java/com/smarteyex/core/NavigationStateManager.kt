package com.smarteyex.core

class NavigationStateManager {

    var currentSection: AppSection = AppSection.DASHBOARD
        private set

    private var onChange: ((AppSection) -> Unit)? = null

    fun setOnChangeListener(listener: (AppSection) -> Unit) {
        onChange = listener
    }

    fun navigateTo(section: AppSection) {
        if (section == currentSection) return
        currentSection = section
        onChange?.invoke(section)
    }
}
