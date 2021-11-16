package com.yurakolesnikov.mooddiary.utils

import androidx.fragment.app.Fragment

// Rounds double to next integer.
fun Double.roundToNextInt (): Int {
    when {
        this == 0.0 -> return 0
        this >= 0.0 -> {
            if (this > this.toInt()) {
                var result = this.toInt() + 1
                return result
            } else {
                var result = this.toInt()
                return result
            }
        }
        else -> throw Exception("Input for function roundToNextInt can't be less than 0")
    }
}