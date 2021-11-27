package com.yurakolesnikov.mooddiary.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.yurakolesnikov.mooddiary.R
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.databinding.FragmentAddNoteBinding
import com.yurakolesnikov.mooddiary.ui.MainActivity.MainActivityViewModel
import com.yurakolesnikov.mooddiary.utils.AutoClearedValue
import com.yurakolesnikov.mooddiary.utils.getCurrentDateTime
import com.yurakolesnikov.mooddiary.utils.toString
import com.yurakolesnikov.mooddiary.utils.hideSystemUI
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddNoteFragment : DialogFragment() {

    private var binding by AutoClearedValue<FragmentAddNoteBinding>(this)

    private val vm: MainActivityViewModel by activityViewModels()

    private val mood: Int get() = binding.autoCompleteTextView.text.toString().toInt()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        dialog?.window?.let {
            it.requestFeature(Window.FEATURE_NO_TITLE) // Removes title of dialog
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Makes bg of dialog
            // transparent to put own drawable with rounded corners.
        }
        hideSystemUI()
        // Assign array of int to drop down view.
        val numbers = resources.getStringArray(R.array.numbers)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdowm_item, numbers)
        binding.autoCompleteTextView.setAdapter(arrayAdapter)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this

    }

    fun onApplyPressed() {
        val sdf = getCurrentDateTime().toString("dd.MM.yyyy")
        parentFragmentManager.beginTransaction().remove(this).commit()
        val newNote = Note(0, sdf, mood)
        vm.insertNote(newNote)
    }

}