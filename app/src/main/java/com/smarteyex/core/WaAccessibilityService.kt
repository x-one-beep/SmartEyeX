package com.smarteyex.core

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class WaAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    fun sendMessage(message: String) {
        val root = rootInActiveWindow ?: return
        val input =
            root.findAccessibilityNodeInfosByViewId(
                "com.whatsapp:id/entry"
            ).firstOrNull() ?: return

        input.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            android.os.Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    message
                )
            }
        )

        val sendBtn =
            root.findAccessibilityNodeInfosByViewId(
                "com.whatsapp:id/send"
            ).firstOrNull()

        sendBtn?.performAction(
            AccessibilityNodeInfo.ACTION_CLICK
        )
    }
}