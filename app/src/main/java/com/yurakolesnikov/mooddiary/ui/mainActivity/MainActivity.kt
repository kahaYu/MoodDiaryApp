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

        vm.allNotesSortedFiltered.observe(this, Observer { notes ->
            when (vm.firstLaunch) {
                true -> {
                    vm.prepopulate(notes)
                    viewPagerAdapter.notifyDataSetChanged()
                    viewPager.setCurrentItem(vm.pages.lastIndex)
                }
                false -> {
                    if (notes.size > 0) {
                        vm.cleanAndInflateAgain(notes)
                    } else {
                        vm.deleteAllPages()
                    }
                }
            }
            syncPagesId()
        })

        // Create page, when current is full.
        //vm.createPageTrigger.observe(this, Observer { note ->
        //    val notesToBeInflated = listOf(note)
        //    createPage(notesToBeInflated)
        //    vm.isFirstLaunch = false
        //})
//
        //// Delete all notes.
        //vm.deleteAllNotesTrigger.observe(this, Observer {
        //    syncPagesId()
        //    NOTE_ID = 0
        //    vm.isFirstLaunch = false
        //    if (vm.sortTriggerNoLiveData) vm.sortTrigger()
        //    if (vm.filterTriggerNoLiveData) vm.filterTrigger()
        //})
//
        //// Insert note.
        //vm.insertNoteTrigger.observe(this, Observer { note ->
        //    if (notesNoLiveData.size == 18) {
        //        return@Observer
        //    }
//
        //    if (vm.pages.isEmpty()) {
        //        vm.createPageTrigger.value = note
        //    } else {
        //        vm.pages.last().inflateNote(note)
        //        viewPager.setCurrentItem(vm.pages.lastIndex)
        //    }
        //    vm.isFirstLaunch = false
//
//
        //})

        // Update note.
        //vm.updateNoteTrigger.observe(this, Observer { note ->
        //    vm.pages[vm.pageFromWhereTapped!!].updateNote(vm.itemViewBinding!!, note.mood)
        //    vm.isFirstLaunch = false
        //})
//
        //// Synchronizing pages id after note deletion.
        //vm.syncPagesIdTrigger.observe(this, Observer { state ->
        //    syncPagesId()
        //})

        // Show undo snackbar
        lifecycleScope.launchWhenStarted {
            vm.event.collect { event ->
                when (event) {
                    is MainActivityViewModel.Event.showUndoDeleteionSnackbar -> {
                        showUndoSnackbar(binding.root, event)
                    }
                    is MainActivityViewModel.Event.syncPagesId -> {
                        syncPagesId()
                    }
                    is MainActivityViewModel.Event.setLastPage -> {
                        viewPager.setCurrentItem(vm.pages.lastIndex)
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



    private fun numberOfPagesNeeded(notesNumber: Int): Int {
        return (notesNumber.toDouble() / 6).roundToNextInt()
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


