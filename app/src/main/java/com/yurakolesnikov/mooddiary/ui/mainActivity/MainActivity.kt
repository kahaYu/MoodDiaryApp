package com.yurakolesnikov.mooddiary.ui.mainActivity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.yurakolesnikov.mooddiary.adapters.ViewPagerAdapter
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.databinding.ActivityMainBinding
import com.yurakolesnikov.mooddiary.sharedViewModels.SharedViewModel
import com.yurakolesnikov.mooddiary.ui.PageFragment
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivityViewModel.Companion.ASC
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivityViewModel.Companion.DSC
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
    private lateinit var notesNoLiveData: List<Note>
    private lateinit var notesAsc: List<Note>
    private lateinit var notesDsc: List<Note>

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

        val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                vm.currentPage = position
            }
        }

        viewPager.registerOnPageChangeCallback(pageChangeCallback)

        // Prepopulation.
        vm.getAllNotes().observe(this, Observer { notes ->
            notesNoLiveData = notes
            notesAsc = notes.sortedBy { note -> note.mood }
            notesDsc = notes.sortedByDescending { note -> note.mood }
            if (vm.isNoteDeletion) {
                if (vm.sortTriggerNoLiveData) {
                    when (vm.sortOrder) {
                        ASC ->  prepopulate(notesAsc)
                        DSC ->  prepopulate(notesDsc)
                    }
                } else prepopulate(notes)
                viewPager.setCurrentItem(vm.currentPage ?: vm.pages.lastIndex)
                vm.isNoteDeletion = false
                showUndoSnackbar(binding.root)
            }
            if (vm.isFirstLaunch) {
                prepopulate(notes)
                viewPager.currentItem = 0
            }
            if (vm.isNoteInsert || vm.isNoteUpdate) {
                if (vm.sortTriggerNoLiveData) vm.sortTrigger.value = true
                vm.isNoteInsert = false
                vm.isNoteUpdate = false
            }
        })

        // Create page, when current is full.
        vm.createPageTrigger.observe(this, Observer { note ->
            val notesToBeInflated = listOf(note)
            createPage(notesToBeInflated)
            vm.isFirstLaunch = false
        })

        // Delete all notes.
        vm.deleteAllNotesTrigger.observe(this, Observer {
            syncPagesId()
            NOTE_ID = 0
            vm.isFirstLaunch = false
            if (vm.sortTriggerNoLiveData) vm.sortTrigger()
        })

        // Insert note.
        vm.insertNoteTrigger.observe(this, Observer { note ->
            if (vm.pages.isEmpty()) {
                vm.createPageTrigger.value = note
            } else {
                vm.pages.last().inflateNote(note)
                viewPager.setCurrentItem(vm.pages.lastIndex)
            }
            vm.isFirstLaunch = false


        })

        // Update note.
        vm.updateNoteTrigger.observe(this, Observer { note ->
            vm.pages[vm.pageFromWhereTapped!!].updateNote(vm.itemViewBinding!!, note.mood)
            vm.isFirstLaunch = false
        })

        // Sort
        vm.sortTrigger.observe(this, Observer { isChecked ->
            if (!vm.pages.isEmpty()) {
                if (isChecked) {
                    vm.removeAllNotesFromScreens()
                    if (vm.sortOrder == ASC) {
                        inflateNotes(notesAsc)
                    } else {
                        inflateNotes(notesDsc)
                    }
                } else {
                    vm.removeAllNotesFromScreens()
                    inflateNotes(notesNoLiveData)
                }
            }
        })

        // Synchronizing pages id after note deletion.
        vm.syncPagesIdTrigger.observe(this, Observer { state ->
            syncPagesId()
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

    private fun deletePage() {
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
        if (!vm.isNoteDeletion) viewPager.setCurrentItem(vm.pages.lastIndex, true)
    }

    private fun numberOfPagesNeeded(notesNumber: Int): Int {
        return (notesNumber.toDouble() / 6).roundToNextInt()
    }



    private fun prepopulate(notes: List<Note>) {
        val numberOfPagesNeeded = (notes.size.toDouble() / 6).roundToNextInt()
        if (notes.size > 0) NOTE_ID = notes.last().id + 1
        if (numberOfPagesNeeded > 0) { // If pages is 0, no need to create.
            val notesToBeInflatedChunked = notes.chunked(6) // Divide by 6 items parts.
            for (page in 1..numberOfPagesNeeded) {
                val notesToBeInflated = notesToBeInflatedChunked[page - 1]
                createPage(notesToBeInflated) // When page is created it knows what to inflate.
            }
            vm.isFirstLaunch = false

        }
    }

    private fun inflateNotes(notes: List<Note>) {
        val notesToBeInflatedChunked = notes.chunked(6)
        for (page in vm.pages) {
            val notesToBeInflated = notesToBeInflatedChunked[vm.pages.indexOf(page)]
            page.inflateNotes(notesToBeInflated)
        }
    }

    fun syncPagesId () {
        viewPagerAdapter.pageIds = viewPagerAdapter.pages.map { it.hashCode().toLong() }
        viewPagerAdapter.notifyDataSetChanged()
    }

    fun showUndoSnackbar (view: View) {
        Snackbar.make(view, "Note deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo", OnClickListener())
            .show()
    }

    companion object {
        var NOTE_ID = 0
    }

    inner class OnClickListener : View.OnClickListener {
        override fun onClick(p0: View?) {
            vm.undoTrigger.value = true
        }
    }
}


