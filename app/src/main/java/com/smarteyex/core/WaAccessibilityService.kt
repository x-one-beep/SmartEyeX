package com.smarteyex.core

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class WaAccessibilityService : AccessibilityService() {

    companion object {
        private var instance: WaAccessibilityService? = null

        fun sendMessage(text: String) {
            instance?.performSend(text)
        }
    }

    override fun onServiceConnected() {
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    private fun performSend(text: String) {
        val root = rootInActiveWindow ?: return

        val input =
            root.findAccessibilityNodeInfosByViewId(
                "com.whatsapp:id/entry"
            ).firstOrNull()

        input?.apply {
            performAction(
                AccessibilityNodeInfo.ACTION_SET_TEXT,
                android.os.Bundle().apply {
                    putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        text
                    )
                }
            )
        }

        val sendBtn =
            root.findAccessibilityNodeInfosByViewId(
                "com.whatsapp:id/send"
            ).firstOrNull()

        sendBtn?.performAction(
            AccessibilityNodeInfo.ACTION_CLICK
        )
    }
}