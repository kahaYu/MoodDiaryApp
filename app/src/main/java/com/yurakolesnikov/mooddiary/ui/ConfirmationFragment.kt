package com.yurakolesnikov.mooddiary.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.yurakolesnikov.mooddiary.R
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.databinding.FragmentAddNoteBinding
import com.yurakolesnikov.mooddiary.databinding.FragmentConfirmationBinding
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivity
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivityViewModel
import com.yurakolesnikov.mooddiary.utils.AutoClearedValue
import com.yurakolesnikov.mooddiary.utils.getCurrentDateTime
import com.yurakolesnikov.mooddiary.utils.toString
import com.yurakolesnikov.mooddiary.utils.hideSystemUI
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class ConfirmationFragment : DialogFragment() {

    private var binding by AutoClearedValue<FragmentConfirmationBinding>(this)

    private val vm: MainActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConfirmationBinding.inflate(inflater, container, false)
        dialog?.window?.let {
            it.requestFeature(Window.FEATURE_NO_TITLE) // Removes title of dialog
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Makes bg of dialog
            // transparent to put own drawable with rounded corners.
        }
        hideSystemUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        binding.vm = vm
    }

    fun onYesPressed() {
        if (vm.isChecked) vm.isAlwaysYes = true
        parentFragmentManager.beginTransaction().remove(this).commit()
        AddNoteFragment().show(parentFragmentManager, "123")
    }

    fun onNoPressed() {
        if (vm.isChecked) vm.isAlwaysNo = true
        parentFragmentManager.beginTransaction().remove(this).commit()
    }
}