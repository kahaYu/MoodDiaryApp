package com.yurakolesnikov.mooddiary.ui.mainActivity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.yurakolesnikov.mooddiary.adapters.ViewPagerAdapter
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.databinding.ActivityMainBinding
import com.yurakolesnikov.mooddiary.sharedViewModels.SharedViewModel
import com.yurakolesnikov.mooddiary.ui.PageFragment
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivityViewModel.Companion.ASC
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivityViewModel.Companion.DSC
import com.yurakolesnikov.mooddiary.ui.mainActivity.MainActivityViewModel.Companion.LESS
import com.yurakolesnikov.mooddiary.utils.hideSystemUI
import com.yurakolesnikov.mooddiary.utils.roundToNextInt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import me.relex.circleindicator.CircleIndicator3

// При добавлении 19-го элемента при включенном фильтре - крэш. <- Проверить это ещё раз.
// Проверить верно ли работает связка фильтр+сортировка.

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

        // Prepopulation.

        vm.getAllNotes().observe(this, Observer { notes ->

            if (notes.size == 19) {
                vm.deleteFirstSixNotes()
                vm.pages.clear()
                syncPagesId()
                vm.isNoteDeletion = true
                return@Observer
            }

            notesNoLiveData = notes

            notesAsc = notes.sortedBy { note -> note.mood }
            notesDsc = notes.sortedByDescending { note -> note.mood }


            if (vm.isNoteDeletion || vm.isUndoDeletion) {
                if (vm.sortTriggerNoLiveData) {
                    when (vm.sortOrder) {
                        ASC ->  prepopulate(notesAsc)
                        DSC ->  prepopulate(notesDsc)
                    }
                } else prepopulate(notes)
                viewPager.setCurrentItem(vm.pageFromWhereTapped ?: vm.pages.lastIndex)
                vm.isNoteDeletion = false
                vm.isUndoDeletion = false

            }
            if (vm.isFirstLaunch) {
                prepopulate(notes)
                viewPager.currentItem = 0
            }
            if (vm.isNoteInsert || vm.isNoteUpdate) {
                if (vm.sortTriggerNoLiveData) vm.sortTrigger.value = true
                if (vm.filterTriggerNoLiveData) vm.filterTrigger.value = true
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
            if (vm.filterTriggerNoLiveData) vm.filterTrigger()
        })

        // Insert note.
        vm.insertNoteTrigger.observe(this, Observer { note ->
            if (notesNoLiveData.size == 18) {
                return@Observer
            }

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

        // Filter
        vm.filterTrigger.observe(this, Observer { isChecked ->
            if (!vm.pages.isEmpty()) {
                if (isChecked) {
                    vm.pages.clear()
                    syncPagesId()
                    if (vm.filterOrder == LESS) {
                        val notesFiltered = notesNoLiveData.filter { it.mood <= vm.threshold }
                        prepopulate(notesFiltered)
                    } else {
                        val notesFiltered = notesNoLiveData.filter { it.mood >= vm.threshold }
                        prepopulate(notesFiltered)
                    }
                } else {
                    vm.pages.clear()
                    syncPagesId()
                    prepopulate(notesNoLiveData)
                }
                viewPager.setCurrentItem(vm.currentPage ?: 0)
            } else prepopulate(notesNoLiveData)

        })

        // Synchronizing pages id after note deletion.
        vm.syncPagesIdTrigger.observe(this, Observer { state ->
            syncPagesId()
        })

        // Show undo snackbar
        lifecycleScope.launchWhenStarted {
            vm.event.collect { event ->
                when (event) {
                    is MainActivityViewModel.Event.showUndoDeleteionSnackbar -> {
                        showUndoSnackbar(binding.root, event)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        editor.apply {
            putBoolean("isAlwaysYes", vm.isAlwaysYes)
            putBoolean("isAlwaysNo", vm.isAlwaysNo)
            apply()
        }
    }

    private fun createPage(notesToBeInflated: List<Note>) {

        vm.pages.add(PageFragment(notesToBeInflated))
        viewPagerAdapter.notifyItemInserted(vm.pages.lastIndex)
        Log.d("Check", "After: there are ${vm.pages.size} pages")
        if (!vm.isNoteDeletion && !vm.isUndoDeletion) {
            viewPager.setCurrentItem(vm.pages.lastIndex, true)
        }
    }

    private fun numberOfPagesNeeded(notesNumber: Int): Int {
        return (notesNumber.toDouble() / 6).roundToNextInt()
    }



    private fun prepopulate(notes: List<Note>) {
        val numberOfPagesNeeded = (notes.size.toDouble() / 6).roundToNextInt()
        if (notes.size > 0) NOTE_ID = notesNoLiveData.last().id + 1
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

    fun showUndoSnackbar (view: View, event: MainActivityViewModel.Event.showUndoDeleteionSnackbar) {
        Snackbar.make(view, "Note deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo", OnClickListener(event))
            .show()
    }

    companion object {
        var NOTE_ID = 0
    }

    inner class OnClickListener (val event: MainActivityViewModel.Event.showUndoDeleteionSnackbar) :
        View.OnClickListener {
        override fun onClick(p0: View?) {
            vm.undoDeleteNote(event.note)
        }
    }
}


