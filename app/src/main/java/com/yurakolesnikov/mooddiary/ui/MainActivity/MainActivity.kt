package com.yurakolesnikov.mooddiary.ui.MainActivity

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.yurakolesnikov.mooddiary.R
import com.yurakolesnikov.mooddiary.adapters.ViewPagerAdapter
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.databinding.ActivityMainBinding
import com.yurakolesnikov.mooddiary.databinding.ItemViewBinding
import com.yurakolesnikov.mooddiary.sharedViewModels.SharedViewModel
import com.yurakolesnikov.mooddiary.ui.AddNoteFragment
import com.yurakolesnikov.mooddiary.ui.PageFragment
import com.yurakolesnikov.mooddiary.utils.hideSystemUI
import com.yurakolesnikov.mooddiary.utils.roundToNextInt
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val vm: MainActivityViewModel by viewModels()
    private val sharedVM: SharedViewModel by viewModels()

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewPager: ViewPager2

    var isFirstLaunch = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)
        hideSystemUI() // Extension that hides system bars.
        supportActionBar?.hide()

        viewPager = binding.viewPagerContainer
        viewPagerAdapter = ViewPagerAdapter(this, vm.pages)
        viewPager.adapter = viewPagerAdapter

        // Prepopulation.
        vm.getAllNotes().observe(this, Observer { notes ->
            val numberOfPagesNeeded = (notes.size.toDouble() / 6).roundToNextInt()
            if (isFirstLaunch && numberOfPagesNeeded > 0) { // If pages is 0, no need to create.
                val notesToBeInflatedChunked = notes.chunked(6) // Divide by 6 items parts.
                for (page in 1..numberOfPagesNeeded) {
                    val notesToBeInflated = notesToBeInflatedChunked[page - 1]
                    createPage(notesToBeInflated) // When page is created it knows what to inflate.
                }
                isFirstLaunch = false
                viewPager.currentItem = 0
            }
        })

        // Create page, when current is full.
        vm.createPageTrigger.observe(this, Observer { note ->
            val notesToBeInflated = listOf(note)
            createPage(notesToBeInflated)
            isFirstLaunch = false
        })

        // Delete all notes.
        vm.deleteAllNotesTrigger.observe(this, Observer {
            viewPagerAdapter.pageIds = viewPagerAdapter.pages.map { it.hashCode().toLong() }
            viewPagerAdapter.notifyDataSetChanged()
        })

        // Insert note.
        vm.insertNoteTrigger.observe(this, Observer { note ->
            if (vm.pages.isEmpty()) {
                vm.createPageTrigger.value = note
            } else {
                vm.pages.last().inflateNote(note)
                viewPager.setCurrentItem(vm.pages.lastIndex)
            }
        })

        // Update note.
        vm.updateNoteTrigger.observe(this, Observer { note ->
            vm.pages[vm.pageFromWhereTapped!!].updateNote(vm.itemViewBinding!!)
        })
    }

    private fun createPage(notesToBeInflated: List<Note>) {
        vm.pages.add(PageFragment(notesToBeInflated))
        viewPagerAdapter.notifyItemInserted(vm.pages.size - 1)
        viewPager.setCurrentItem(vm.pages.lastIndex, true)
    }

    private fun numberOfPagesNeeded(notesNumber: Int): Int {
        return (notesNumber.toDouble() / 6).roundToNextInt()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }
}


