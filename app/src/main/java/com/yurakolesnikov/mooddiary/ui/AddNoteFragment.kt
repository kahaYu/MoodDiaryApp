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
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivity
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivityViewModel
import com.yurakolesnikov.mooddiary.utils.AutoClearedValue
import com.yurakolesnikov.mooddiary.utils.getCurrentDateTime
import com.yurakolesnikov.mooddiary.utils.toString
import com.yurakolesnikov.mooddiary.utils.hideSystemUI
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddNoteFragment(
    val text: String = "Rate your happiness today", // By default it's adding dialog, not updating
    val note: Note? = null
) : DialogFragment() {

    private var binding
            by AutoClearedValue<FragmentAddNoteBinding>(this) // Delegate to manage lifecycle of binding object

    private val vm: MainActivityViewModel by activityViewModels() // Obtain the same view model like in activity

    private val mood: Int get() = binding.autoCompleteTextView.text.toString().toInt() // Always up to date mood

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        dialog?.window?.let {
            it.requestFeature(Window.FEATURE_NO_TITLE) // Remove title of dialog
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Make bg of dialog
            // transparent to put own drawable with rounded corners
        }
        hideSystemUI()

        val numbers = resources.getStringArray(R.array.numbers) // Assign array of int to drop down view
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdowm_item, numbers)
        binding.autoCompleteTextView.setAdapter(arrayAdapter)

        return binding.root
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvRateYourHappiness.text = text

        binding.vm = vm
        binding.fragment = this
        binding.lifecycleOwner = this
        binding.isAddDialog = note == null // Property to manage appearance of delete button in XML

        var image: Drawable = resources.getDrawable(R.drawable.emoji_1_3) // Default image when its adding dialog

        if (note == null) // Means it's adding dialog
            binding.autoCompleteTextView
                .setText("1", false)
                .also { vm.previewImage.value = image } // Default drop-down number is 1 and crying image
        else { // Means it's updating dialog. Have to pull data from clicked note
            binding.autoCompleteTextView
                .setText(note.mood.toString(), false) // Set number of drop-down to mood of clicked note

            image = vm.selectImage(note.mood) // Set mini-image depending on mood of clicked note
        }

        binding.autoCompleteTextView.addTextChangedListener(object : TextWatcher { // Update mini-image real-time
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                vm.previewImage.value =
                    vm.selectImage(p0.toString().toInt()) // Fire live data to update mini-image in XML
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    fun onApplyPressed() {
        val currentDate = getCurrentDateTime().toString("dd.MM.yyyy")
        if (note == null) { // Means its adding dialog
            val newNote = Note(currentDate, mood)
            vm.insertNote(newNote) // Add note to database
        } else { // Means its updating dialog
            note.mood = mood
            vm.updateNote(note) // Update existing note in database
        }
        parentFragmentManager.beginTransaction().remove(this).commit() // Close dialog
    }

    fun onDeleteNotePressed() {
        if (note != null) { // Means its updating dialog
            vm.deleteNote(note)
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
    }

}