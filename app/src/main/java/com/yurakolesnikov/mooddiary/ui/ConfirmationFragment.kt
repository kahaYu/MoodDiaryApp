package com.yurakolesnikov.mooddiary.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.yurakolesnikov.mooddiary.databinding.FragmentConfirmationBinding
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivityViewModel
import com.yurakolesnikov.mooddiary.utils.AutoClearedValue
import com.yurakolesnikov.mooddiary.utils.hideSystemUI
import dagger.hilt.android.AndroidEntryPoint

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
            it.requestFeature(Window.FEATURE_NO_TITLE)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
        if (vm.dontAskAgainChecked) vm.isAlwaysYes = true // Remember YES forever
        parentFragmentManager.beginTransaction().remove(this).commit()
        AddNoteFragment().show(parentFragmentManager, "AddNoteFragment")
    }

    fun onNoPressed() {
        if (vm.dontAskAgainChecked) vm.isAlwaysNo = true // Remember NO forever
        parentFragmentManager.beginTransaction().remove(this).commit()
    }
}