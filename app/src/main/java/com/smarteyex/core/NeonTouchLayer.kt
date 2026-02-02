package com.smarteyex.core.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import kotlin.math.min

class NeonTouchLayer(context: Context) : View(context) {

    enum class Mode {
        CORE,
        CHAT,
        CAMERA,
        MEMORY,
        SETTINGS
    }

    private var mode: Mode = Mode.CORE
    private var active = true
    private var lowPower = false

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    private var touchX = -1f
    private var touchY = -1f
    private var radius = 0f

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    fun setMode(mode: Mode) {
        this.mode = mode
    }

    fun setActive(active: Boolean) {
        this.active = active
        invalidate()
    }

    fun setLowPower(low: Boolean) {
        this.lowPower = low
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!active) return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            touchX = event.x
            touchY = event.y
            radius = if (lowPower) 60f else 120f
            invalidate()
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (touchX < 0 || !active) return

        paint.color = when (mode) {
            Mode.CORE -> 0x55A0E9FF.toInt()
            Mode.CHAT -> 0x553A6EA5.toInt()
            Mode.CAMERA -> 0x5548FF9C.toInt()
            Mode.MEMORY -> 0x55C77DFF.toInt()
            Mode.SETTINGS -> 0x33444444
        }

        canvas.drawCircle(
            touchX,
            touchY,
            min(radius, 200f),
            paint
        )

        radius *= 0.92f
        if (radius > 5f) invalidate()
        else {
            touchX = -1f
        }
    }
}