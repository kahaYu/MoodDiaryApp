package com.yurakolesnikov.mooddiary.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.yurakolesnikov.mooddiary.database.model.Note
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

    private lateinit var lastNote: Note

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

        vm.getAllNotes().observe(viewLifecycleOwner, Observer { notes ->
            if (notes.size > 0) {
                lastNote = notes.last()
            } else lastNote = Note("00.00.0000", 1)
        })

    }

    fun onAddPressed () {
        val sdf = getCurrentDateTime().toString("dd.MM.yyyy")
         if (lastNote.date != sdf) {AddNoteFragment().show(parentFragmentManager, "123")
        } else
             ConfirmationFragment().show(parentFragmentManager, "ConfirmationFragment")
    }

    fun onDeleteAllPressed () {
        vm.deleteAllNotes()
    }

    fun onSortPressed () {
        // TO DO
    }

    fun onFilterPressed () {
        // TO DO
    }

}