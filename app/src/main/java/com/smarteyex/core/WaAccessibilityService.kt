package com.smarteyex.core.wa

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class WaAccessibilityService: AccessibilityService() {

    companion object{
        var pendingText=""
        var sendNow=false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if(!sendNow) return

        rootInActiveWindow?.findAccessibilityNodeInfosByText("Type a message")
            ?.firstOrNull()?.apply{
                performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,
                    android.os.Bundle().apply{
                        putCharSequence(
                          AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                          pendingText)
                    })
            }

        rootInActiveWindow?.findAccessibilityNodeInfosByText("Send")
            ?.firstOrNull()?.performAction(
                AccessibilityNodeInfo.ACTION_CLICK)

        sendNow=false
    }

    override fun onInterrupt() {}
}