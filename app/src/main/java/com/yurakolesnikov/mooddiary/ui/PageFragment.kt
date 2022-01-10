package com.yurakolesnikov.mooddiary.ui

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.yurakolesnikov.mooddiary.R
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.databinding.FragmentPageBinding
import com.yurakolesnikov.mooddiary.databinding.ItemViewBinding
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivity
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivityViewModel
import com.yurakolesnikov.mooddiary.utils.AutoClearedValue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PageFragment(private val notesToBeInflated: List<Note>) : Fragment() {

    var binding by AutoClearedValue<FragmentPageBinding>(this)

    private val vm: MainActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,

        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPageBinding.inflate(layoutInflater, null, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inflateNotes(notesToBeInflated) // Populate notes here with respect of lifecycle

    }

    override fun onResume() {
        super.onResume()
        vm.currentPage = vm.pages.indexOf(this) // Track of current page to pass to view pager to navigate properly
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun inflateNote(note: Note) {

        val view = LayoutInflater.from(requireContext()).inflate( // Obtain one item view
            R.layout.item_view,
            null, false
        )

        val itemviewBinding = ItemViewBinding.bind(view) // Obtain binding object of this item view
        itemviewBinding.fragment = this
        itemviewBinding.note = note

        val image: Drawable = vm.selectImage(note.mood) // Select image depending on mood

        itemviewBinding.apply {
            tvMoodRating.text = note.mood.toString()
            tvCurrentDate.text = note.date
            imageView.setImageDrawable(image)
        }

        attachItemViewToContainer(binding, view, note)

    }

    fun onItemClick(note: Note) { // Open updating dialog
        AddNoteFragment("Change your mood", note)
            .show(parentFragmentManager, "AddNoteFragment")
    }

    fun removeAllNotes() { // Clear page from all views
        binding.item1.removeAllViews()
        binding.item2.removeAllViews()
        binding.item3.removeAllViews()
        binding.item4.removeAllViews()
        binding.item5.removeAllViews()
        binding.item6.removeAllViews()
    }

    fun inflateNotes(notesToBeInflated: List<Note>) {
        for (note in notesToBeInflated) {
            inflateNote(note) // Inflating note needed
        }
    }

    private fun attachItemViewToContainer(binding: FragmentPageBinding, view: View, note: Note) {

        when { // Check every container for emptiness. If empty - add view there
            binding.item1.childCount == 0 -> binding.item1.addView(view)
            binding.item2.childCount == 0 -> binding.item2.addView(view)
            binding.item3.childCount == 0 -> binding.item3.addView(view)
            binding.item4.childCount == 0 -> binding.item4.addView(view)
            binding.item5.childCount == 0 -> binding.item5.addView(view)
            binding.item6.childCount == 0 -> binding.item6.addView(view)
            else -> { // Means current page contains 6 items already
                val note = listOf(note)
                vm.createPage(note) // Create new page when current is full
            }
        }
    }
}

