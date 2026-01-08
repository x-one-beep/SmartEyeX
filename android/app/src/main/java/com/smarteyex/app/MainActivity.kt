package com.smarteyex.app

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this)
        tv.text = "SmartEyeX Active"
        tv.textSize = 22f
        tv.setPadding(40, 80, 40, 40)

        setContentView(tv)
    }
}
