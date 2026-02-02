package com.smarteyex.core

object WaSender {

    fun send(text: String) {
        WaAccessibilityService.sendMessage(text)
    }
}