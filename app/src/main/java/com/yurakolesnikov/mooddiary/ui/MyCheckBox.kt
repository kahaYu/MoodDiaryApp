package com.yurakolesnikov.mooddiary.ui

import android.R
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet

import android.widget.CheckBox


class MyCheckBox(context: Context, attrs: AttributeSet?) :
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
