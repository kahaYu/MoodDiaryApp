package com.yurakolesnikov.mooddiary.ui.mainActivity

import android.R
import android.content.Context
import android.content.SharedPreferences
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.GONE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.yurakolesnikov.mooddiary.adapters.ViewPagerAdapter
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.databinding.ActivityMainBinding
import com.yurakolesnikov.mooddiary.sharedViewModels.SharedViewModel
import com.yurakolesnikov.mooddiary.ui.PageFragment
import com.yurakolesnikov.mooddiary.utils.hideSystemUI
import com.yurakolesnikov.mooddiary.utils.roundToNextInt
import dagger.hilt.android.AndroidEntryPoint
import me.relex.circleindicator.CircleIndicator3


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val vm: MainActivityViewModel by viewModels()
    private val sharedVM: SharedViewModel by viewModels()

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewPager: ViewPager2

    var isFirstLaunch = true

    private lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)
        hideSystemUI() // Extension that hides system bars.
        supportActionBar?.hide()

        viewPager = binding.viewPagerContainer
        viewPagerAdapter = ViewPagerAdapter(this, vm.pages)
        viewPager.adapter = viewPagerAdapter

        val indicator: CircleIndicator3 = binding.indicator
        indicator.setViewPager(viewPager)
        viewPagerAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver())

        sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        editor = sharedPref.edit()

        vm.isAlwaysYes = sharedPref.getBoolean("isAlwaysYes", false)
        vm.isAlwaysNo = sharedPref.getBoolean("isAlwaysNo", false)

        // Prepopulation.
        vm.getAllNotes().observe(this, Observer { notes ->

            //if (notes.size > 18) vm.deleteFirstSixNotes()

            val numberOfPagesNeeded = (notes.size.toDouble() / 6).roundToNextInt()
            if (notes.size > 0) NOTE_ID = notes.last().id + 1
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
            NOTE_ID = 0
            isFirstLaunch = false
        })

        // Insert note.
        vm.insertNoteTrigger.observe(this, Observer { note ->
            if (vm.pages.isEmpty()) {
                vm.createPageTrigger.value = note
            } else {
                vm.pages.last().inflateNote(note)
                viewPager.setCurrentItem(vm.pages.lastIndex)
            }
            isFirstLaunch = false

        })

        // Update note.
        vm.updateNoteTrigger.observe(this, Observer { note ->
            vm.pages[vm.pageFromWhereTapped!!].updateNote(vm.itemViewBinding!!, note.mood)
            isFirstLaunch = false
        })

    }

    override fun onStop() {
        super.onStop()
        editor.apply {
            putBoolean("isAlwaysYes", vm.isAlwaysYes)
            putBoolean("isAlwaysNo", vm.isAlwaysNo)
            apply()
        }
    }

    private fun deletePage () {
        vm.deleteFirstSixNotes()
        vm.pages.removeAt(0)
        viewPagerAdapter.notifyItemRemoved(0)
        viewPager.setCurrentItem(vm.pages.lastIndex, true)
    }

    private fun createPage(notesToBeInflated: List<Note>) {

        if (vm.pages.size == 3) deletePage()

        vm.pages.add(PageFragment(notesToBeInflated))
        viewPagerAdapter.notifyItemInserted(vm.pages.lastIndex)
        Log.d("Check", "After: there are ${vm.pages.size} pages")
        viewPager.setCurrentItem(vm.pages.lastIndex, true)
    }

    private fun numberOfPagesNeeded(notesNumber: Int): Int {
        return (notesNumber.toDouble() / 6).roundToNextInt()
    }


    companion object {
        var NOTE_ID = 0
    }
}


