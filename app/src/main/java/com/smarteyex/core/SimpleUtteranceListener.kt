package com.smarteyex.core

import android.speech.tts.UtteranceProgressListener

class SimpleUtteranceListener(
    private val onDone: () -> Unit,
    private val onError: () -> Unit
) : UtteranceProgressListener() {

    override fun onStart(utteranceId: String?) {}

    override fun onDone(utteranceId: String?) {
        onDone.invoke()
    }

    override fun onError(utteranceId: String?) {
        onError.invoke()
    }
}