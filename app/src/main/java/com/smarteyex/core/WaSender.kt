package com.smarteyex.core

object WaSender {

    fun send(message: String) {
        val service =
            ServiceController.waAccessibility
                ?: return

        service.sendMessage(message)
    }
}