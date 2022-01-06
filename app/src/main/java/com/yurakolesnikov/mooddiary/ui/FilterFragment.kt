package com.yurakolesnikov.mooddiary.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.yurakolesnikov.mooddiary.R
import com.yurakolesnikov.mooddiary.databinding.FragmentFilterBinding
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivityViewModel
import com.yurakolesnikov.mooddiary.utils.AutoClearedValue
import com.yurakolesnikov.mooddiary.utils.FilterOrder
import com.yurakolesnikov.mooddiary.utils.hideSystemUI
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilterFragment : DialogFragment() {

    private var binding by AutoClearedValue<FragmentFilterBinding>(this)

    private val vm: MainActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFilterBinding.inflate(inflater, container, false)
        dialog?.window?.let {
            it.requestFeature(Window.FEATURE_NO_TITLE) // Removes title of dialog
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Makes bg of dialog
            // transparent to put own drawable with rounded corners.
        }
        hideSystemUI()
        val numbers = resources.getStringArray(R.array.numbers)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdowm_item, numbers)
        binding.autoCompleteTextView.setAdapter(arrayAdapter)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        binding.vm = vm
        binding.lifecycleOwner = this
        binding.buttonMoreLess.isChecked = if (vm.filterOrder == FilterOrder.LESS ) true else false

        if (vm.filterChecked) {
            binding.autoCompleteTextView.setText(vm.threshold.toString(), false)}
        else vm.threshold = 1


        binding.autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (vm.filterChecked) vm.thresholdLD.value = p0.toString().toInt()
                else vm.threshold = p0.toString().toInt()

            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

}