package com.yurakolesnikov.mooddiary.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.yurakolesnikov.mooddiary.R
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.databinding.FragmentAddNoteBinding
import com.yurakolesnikov.mooddiary.ui.MainActivity.MainActivity
import com.yurakolesnikov.mooddiary.ui.MainActivity.MainActivityViewModel
import com.yurakolesnikov.mooddiary.utils.AutoClearedValue
import com.yurakolesnikov.mooddiary.utils.getCurrentDateTime
import com.yurakolesnikov.mooddiary.utils.toString
import com.yurakolesnikov.mooddiary.utils.hideSystemUI
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddNoteFragment(
    val text: String = "Rate your happiness today",
    val initialMood: Int = 1,
    val note: Note? = null
) : DialogFragment() {

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
        binding.tvRateYourHappiness.text = text
        binding.autoCompleteTextView.setText(initialMood.toString(), false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        binding.vm = vm
        binding.lifecycleOwner = this

        binding.autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val image: Drawable = when {
                    p0.toString().toInt() <= 3 -> resources.getDrawable(R.drawable.emoji_1_3)
                    p0.toString().toInt() <= 6 -> resources.getDrawable(R.drawable.emoji_4_6)
                    p0.toString().toInt() <= 9 -> resources.getDrawable(R.drawable.emoji_7_9)
                    else -> resources.getDrawable(R.drawable.emoji_10)
                }
                vm.setPreviewImage(image)
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    fun onApplyPressed() {
        val sdf = getCurrentDateTime().toString("dd.MM.yyyy")
        if (note == null) {
            val newNote = Note(sdf, mood, MainActivity.NOTE_ID)
            MainActivity.NOTE_ID++
            vm.insertNote(newNote)
        } else {
            note.mood = mood
            val id = note.id
            vm.updateNote(note)
        }
        parentFragmentManager.beginTransaction().remove(this).commit()
    }

}