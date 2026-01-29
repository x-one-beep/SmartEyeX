package com.smarteyex.core.wa

object WaSender {
    fun send(text:String){
        WaAccessibilityService.pendingText = text
        WaAccessibilityService.sendNow = true
    }
}