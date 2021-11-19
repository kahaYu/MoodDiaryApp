package com.yurakolesnikov.mooddiary.sharedViewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    val onAddPressed = MutableLiveData<Boolean>()
    fun onAddPressed() { onAddPressed.value = true }
}