package com.yurakolesnikov.mooddiary.ui

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.DialogFragment
import com.yurakolesnikov.mooddiary.R
import com.yurakolesnikov.mooddiary.databinding.FragmentAddNoteBinding
import com.yurakolesnikov.mooddiary.utils.AutoClearedValue
import com.yurakolesnikov.mooddiary.utils.hideSystemUI

class AddNoteFragment : DialogFragment() {

    private var binding by AutoClearedValue<FragmentAddNoteBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        getDialog()?.getWindow()?.requestFeature(Window.FEATURE_NO_TITLE) // Removes title of dialog
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val numbers = resources.getStringArray(R.array.numbers)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdowm_item, numbers)
        binding.autoCompleteTextView.setAdapter(arrayAdapter)
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }
}