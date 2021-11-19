package com.yurakolesnikov.mooddiary.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.yurakolesnikov.mooddiary.R
import com.yurakolesnikov.mooddiary.databinding.FragmentNavBarBinding
import com.yurakolesnikov.mooddiary.sharedViewModels.SharedViewModel
import com.yurakolesnikov.mooddiary.utils.AutoClearedValue

class NavBarFragment : Fragment() {

    private var binding by AutoClearedValue<FragmentNavBarBinding>(this)

    private val sharedVM: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNavBarBinding.inflate(inflater, null, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Need to remove bg here, because if to set transparent color in XML, shadow remains.
        binding.apply {
            buttonDeleteAll.background = null
            buttonSort.background = null
            buttonFilter.background = null
        }

        binding.fragment = this

    }

    fun onAddPressed () {
        sharedVM.onAddPressed()
    }

    fun onDeleteAllPressed () {
        // TO DO
    }

    fun onSortPressed () {
        // TO DO
    }

    fun onFilterPressed () {
        // TO DO
    }

}