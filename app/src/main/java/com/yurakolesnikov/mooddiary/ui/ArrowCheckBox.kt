package com.yurakolesnikov.mooddiary.ui

import android.R
import android.R.*
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Layout
import android.util.AttributeSet

import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet


class ArrowCheckBox(context: Context, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatCheckBox(context, attrs) {

    override fun setChecked(t: Boolean) {
        if (t) {
            background = resources.getDrawable(com.yurakolesnikov.mooddiary.R.drawable.arrow_down1)
        } else {
            background = resources.getDrawable(com.yurakolesnikov.mooddiary.R.drawable.arrow_up1)
        }
        super.setChecked(t)
    }
}
