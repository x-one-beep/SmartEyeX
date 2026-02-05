package com.smarteyex.emotion

object AttachmentCore {

    private var level = 5 // 1 - asing, 10 - partner hidup

    fun increase() {
        if (level < 10) level++
    }

    fun decrease() {
        if (level > 1) level--
    }

    fun get(): Int = level
}