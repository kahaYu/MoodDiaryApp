package com.yurakolesnikov.mooddiary.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.yurakolesnikov.mooddiary.databinding.FragmentNavBarBinding
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivityViewModel
import com.yurakolesnikov.mooddiary.utils.AutoClearedValue
import com.yurakolesnikov.mooddiary.utils.getCurrentDateTime
import com.yurakolesnikov.mooddiary.utils.toString
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NavBarFragment : Fragment() {

    private var binding by AutoClearedValue<FragmentNavBarBinding>(this)

    private val vm: MainActivityViewModel by activityViewModels()

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

        removeBackground(binding) // Remove bg here, cause if to set transparent color in XML, shadow remains.

        binding.fragment = this
        binding.lifecycleOwner = this
        binding.vm = vm
    }

    fun onAddPressed() {
        val currentDate = getCurrentDateTime().toString("dd.MM.yyyy")
        val lastDate = if (vm.notesNoLd.isEmpty())
        if (vm.notesNoLd.isEmpty())
        if (vm.notesNoLd.last().date != currentDate || vm.isAlwaysYes) { // Show add dialog if now is new day or user
            AddNoteFragment().show(parentFragmentManager, "AddNoteFragment") // chose remember always YES
        } else if (vm.notesNoLd.last().date == currentDate && vm.isAlwaysNo) {
            Toast.makeText(requireContext(), "Try again tomorrow", Toast.LENGTH_SHORT).show()
        } else
            ConfirmationFragment().show(parentFragmentManager, "Conf fragment")
    }

    fun onDeleteAllPressed() {
        vm.deleteAllNotes()
    }

    fun onSortPressed() {
        SortFragment().show(parentFragmentManager, "Sort fragment") // Show sort dialog
    }

    fun onFilterPressed() {
        FilterFragment().show(parentFragmentManager, "Filter fragment") // Show filter dialog
    }

    private fun removeBackground(binding: FragmentNavBarBinding) {
        binding.apply {
            buttonDeleteAll.background = null
            buttonSort.background = null
            buttonFilter.background = null
        }
    }
}