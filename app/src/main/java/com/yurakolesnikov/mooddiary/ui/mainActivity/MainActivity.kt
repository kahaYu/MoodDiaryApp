package com.yurakolesnikov.mooddiary.ui.mainActivity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.yurakolesnikov.mooddiary.adapters.ViewPagerAdapter
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.databinding.ActivityMainBinding
import com.yurakolesnikov.mooddiary.ui.PageFragment
import com.yurakolesnikov.mooddiary.utils.hideSystemUI
import com.yurakolesnikov.mooddiary.utils.roundToNextInt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import me.relex.circleindicator.CircleIndicator3

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val vm: MainActivityViewModel by viewModels() // Init with help of delegate

    private lateinit var viewPagerAdapter: ViewPagerAdapter // Use lateinit to avoid null-checks
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)
        hideSystemUI() // Extension hides system bars
        supportActionBar?.hide()

        viewPager = binding.viewpagerContainer
        viewPagerAdapter = ViewPagerAdapter(this, vm.pages)
        viewPager.adapter = viewPagerAdapter

        val indicator: CircleIndicator3 = binding.indicator
        indicator.setViewPager(viewPager)
        viewPagerAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver())

        // Observe LiveData. Receive list of notes for rendering
        vm.allNotesSortedFiltered.observe(this, Observer { notes ->
            when (vm.firstLaunch) {
                true -> {
                    vm.prepopulate(notes) // Recover previous state of ui
                    viewPagerAdapter.notifyDataSetChanged()
                    viewPager.setCurrentItem(vm.pages.lastIndex)
                }
                false -> {
                    if (notes.isNotEmpty()) vm.cleanAndInflateAgain(notes) // Clean previous state of ui and set new one
                    else vm.deleteAllPages() // If incoming list is empty, delete pages
                }
            }
            syncPagesId() // After all sync pages ids for proper adapter work
        })

        // Collect single-life events
        lifecycleScope.launchWhenStarted { // Use .launchWhenStarted{} to scope collecting Start->Stopped.
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
                    is MainActivityViewModel.Event.showToastNotesLimit -> {
                        Toast.makeText(applicationContext, "Notes limit is exceed. First 6 notes are " +
                                "deleted",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        vm.spEditor.apply {
            putBoolean("isAlwaysYes", vm.isAlwaysYes)
            putBoolean("isAlwaysNo", vm.isAlwaysNo)
            apply() // Faster than .commit(), cause asynchronous
        }
    }

    fun syncPagesId () {
        viewPagerAdapter.pageIds = vm.pages.map { it.hashCode().toLong() } // Sync fragments ids for proper adapter work
        viewPagerAdapter.notifyDataSetChanged()
    }

    fun showUndoSnackbar (view: View, event: MainActivityViewModel.Event.showUndoDeleteionSnackbar) {
        Snackbar.make(view, "Note deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo", OnClickListener(event))
            .show()
    }

    inner class OnClickListener (val event: MainActivityViewModel.Event.showUndoDeleteionSnackbar) :
        View.OnClickListener {
        override fun onClick(p0: View?) {
            vm.undoDeleteNote(event.note)
        }
    }

    companion object {
        var NOTE_ID = 0
    }
}


