package com.yurakolesnikov.mooddiary.ui.customUi

import android.content.Context
import android.util.AttributeSet


class ArrowCheckBox(context: Context, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatCheckBox(context, attrs) {

    override fun setChecked(t: Boolean) {
        if (t) {
            background = resources.getDrawable(com.yurakolesnikov.mooddiary.R.drawable.arrow_down)
        } else {
            background = resources.getDrawable(com.yurakolesnikov.mooddiary.R.drawable.arrow_up)
        }
        super.setChecked(t)
    }
}
