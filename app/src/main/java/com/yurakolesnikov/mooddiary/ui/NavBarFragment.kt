package com.yurakolesnikov.mooddiary.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import com.yurakolesnikov.mooddiary.R
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.databinding.FragmentNavBarBinding
import com.yurakolesnikov.mooddiary.sharedViewModels.SharedViewModel
import com.yurakolesnikov.mooddiary.ui.MainActivity.MainActivityViewModel
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
            lastNote = notes.last()
        })

    }

    fun onAddPressed () {
        val sdf = getCurrentDateTime().toString("dd.MM.yyyy")
        if (lastNote.date != sdf) {
            AddNoteFragment().show(parentFragmentManager, "123")
        } else
            Toast.makeText(requireContext(), "You've already rated your today's mood." +
                    "\nTry again tomorrow.", Toast.LENGTH_SHORT).show()

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