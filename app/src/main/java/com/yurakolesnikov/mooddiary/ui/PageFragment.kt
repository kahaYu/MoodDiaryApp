package com.yurakolesnikov.mooddiary.ui

import android.annotation.SuppressLint
import android.content.Context
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
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivityViewModel
import com.yurakolesnikov.mooddiary.utils.AutoClearedValue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PageFragment(private val notesToBeInflated: List<Note>) : Fragment() {

    var binding by AutoClearedValue<FragmentPageBinding>(this)

    private val vm: MainActivityViewModel by activityViewModels()

    private lateinit var notesNoLiveData: List<Note>

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

        inflateNotes(notesToBeInflated)

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun inflateNote(note: Note) {

        val view = LayoutInflater.from(requireContext()).inflate(
            R.layout.item_view,
            null, false
        )

        val viewBinding = ItemViewBinding.bind(view)
        viewBinding.fragment = this
        viewBinding.note = note
        viewBinding.mood = note.mood
        viewBinding.itemViewBinding = viewBinding
        viewBinding.fragment = this


        val image: Drawable = when {
            note.mood <= 3 -> resources.getDrawable(R.drawable.emoji_1_3)
            note.mood <= 6 -> resources.getDrawable(R.drawable.emoji_4_6)
            note.mood <= 9 -> resources.getDrawable(R.drawable.emoji_7_9)
            else -> resources.getDrawable(R.drawable.emoji_10)
        }
        viewBinding.apply {
            tvMoodRating.text = note.mood.toString()
            tvCurrentDate.text = note.date
            imageView.setImageDrawable(image)
        }

        if (binding.item1.childCount == 0) {
            binding.item1.addView(view)
        }
            else if (binding.item2.childCount == 0) {
            binding.item2.addView(view)
                } else if (binding.item3.childCount == 0) {
            binding.item3.addView(view)
                } else if (binding.item4.childCount == 0) {
            binding.item4.addView(view)
                } else if (binding.item5.childCount == 0) {
            binding.item5.addView(view)
                } else if (binding.item6.childCount == 0) {
            binding.item6.addView(view)
                }
        else {
            vm.createPageTrigger.value = note // Create new page when current is full.
        }
    }

    fun updateNote(itemViewBinding: ItemViewBinding, mood: Int) {
        val image: Drawable = when {
            mood <= 3 -> resources.getDrawable(R.drawable.emoji_1_3)
            mood <= 6 -> resources.getDrawable(R.drawable.emoji_4_6)
            mood <= 9 -> resources.getDrawable(R.drawable.emoji_7_9)
            else -> resources.getDrawable(R.drawable.emoji_10)
        }
        itemViewBinding.apply {
            tvMoodRating.text = mood.toString()
            imageView.setImageDrawable(image)
        }
    }

    fun onItemClick(note: Note, mood: Int, viewBinding: ViewDataBinding, page: PageFragment) {
        AddNoteFragment("Change your mood", note)
            .show(parentFragmentManager, "456")
        vm.itemViewBinding = viewBinding as ItemViewBinding?
        vm.pageFromWhereTapped = vm.pages.indexOf(page)
    }

    fun removeAllNotes () {
        binding.item1.removeAllViews()
        binding.item2.removeAllViews()
        binding.item3.removeAllViews()
        binding.item4.removeAllViews()
        binding.item5.removeAllViews()
        binding.item6.removeAllViews()
    }

    fun inflateNotes(notesToBeInflated: List<Note>) {
        for (note in notesToBeInflated) {
            inflateNote(note) // Inflating notes needed.
        }
    }

    companion object {
        var allNotes: List<Note>? = null
    }
}

