package com.yurakolesnikov.mooddiary.ui.mainActivity

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.lifecycle.*
import com.yurakolesnikov.mooddiary.R
import com.yurakolesnikov.mooddiary.database.Dao
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.databinding.ItemViewBinding
import com.yurakolesnikov.mooddiary.ui.PageFragment
import com.yurakolesnikov.mooddiary.utils.FilterOrder
import com.yurakolesnikov.mooddiary.utils.Notes
import com.yurakolesnikov.mooddiary.utils.SortOrder
import com.yurakolesnikov.mooddiary.utils.roundToNextInt
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val dao: Dao,
    private val sp: SharedPreferences,
    val spEditor: SharedPreferences.Editor,
    private val resources: Resources
) : ViewModel() {

    var firstLaunch = true // Indicates if app's just been launched

    var isAlwaysYes = sp.getBoolean("isAlwaysYes", false) // Settings of confirmation dialog
    var isAlwaysNo = sp.getBoolean("isAlwaysNo", false)
    var dontAskAgainChecked = false // Checkbox to remember chosen option

    private var sortOrderLd = MutableLiveData<Int>()
    var sortOrder = SortOrder.ASC // Default state of sorting order
    var sortCheckedLd = MutableLiveData<Boolean>(false) // Not private cause is called from XML
    var sortChecked = false // By default sorting is off

    private var filterOrderLd = MutableLiveData<Int>()
    var filterOrder = FilterOrder.MORE // Default state of filtering direction
    var filterCheckedLd = MutableLiveData<Boolean>(false) // Not private cause is called from XML
    var filterChecked = false // By default filtering is off

    var thresholdLd = MutableLiveData<Int>() // Threshold for filtering
    var threshold = 1 // Default state of threshold

    val pages = mutableListOf<PageFragment>() // Default empty list of pages for viewPager

    var currentPage: Int = 0 // To track current page to keep right position of view pager

    var notesNoLd = mutableListOf<Note>() // Property to keep list of all notes after each change

    var previewImage = MutableLiveData<Drawable>() // Mini-image to show in add note dialog. Observed in XML

    private val eventChannel = Channel<Event>()
    val event = eventChannel.receiveAsFlow()

    sealed class Event { // Kinds of single-life events for ui
        data class ShowUndoDeletionSnackbar(val note: Note) : Event()
        object SyncPagesId : Event()
        object SetLastPage : Event()
        object ShowToastNotesLimit : Event()
        object SetCurrentPage : Event()
    }

    val allNotesSortedFiltered = object : MediatorLiveData<List<Note>>() { // Notes for ui

        init { // Sources to combine in one live data to be observed from main activity
            addSource(getAllNotes()) { notes ->
                notesNoLd = notes as MutableList<Note>
                getAllNotesSortedFiltered()
            }
            addSource(sortOrderLd) { sortOrderValue ->
                sortOrder = sortOrderValue
                getAllNotesSortedFiltered()
            }
            addSource(sortCheckedLd) { sortCheckedValue ->
                sortChecked = sortCheckedValue
                getAllNotesSortedFiltered()
            }
            addSource(filterOrderLd) { filterOrderValue ->
                filterOrder = filterOrderValue
                getAllNotesSortedFiltered()
            }
            addSource(filterCheckedLd) { filterCheckedValue ->
                filterChecked = filterCheckedValue
                getAllNotesSortedFiltered()
            }
            addSource(thresholdLd) { thresholdValue ->
                threshold = thresholdValue
                getAllNotesSortedFiltered()
            }
        }

        private fun getAllNotesSortedFiltered() { // Emit prepared for ui list of notes as value of mediator live data
            viewModelScope.launch {
                value = allNotesSortedFiltered(
                    notesNoLd, sortOrder, sortChecked, filterOrder, filterChecked, threshold
                )
            }
        }
    }

    fun insertNote(note: Note) {
        viewModelScope.launch { // 3 pages are max in viewPager. If 19th note is added, first page of notes is deleted
            if (notesNoLd.size == 18) deleteFirstSixNotes()
                .also { eventChannel.send(Event.ShowToastNotesLimit) } // And user gets notification
            dao.insertNote(note)
            eventChannel.send(Event.SetLastPage) // Always move to last page when new note is added
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch { dao.updateNote(note) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            dao.deleteNote(note)
            eventChannel.send(Event.ShowUndoDeletionSnackbar(note)) // User could undo deletion
        }
    }

    fun undoDeleteNote(note: Note) {
        viewModelScope.launch {
            dao.insertNote(note)
        }
    }

    fun deleteFirstSixNotes() {
        viewModelScope.launch { dao.deleteFirstSixNotes() }
    } // When first page has to be deleted

    fun deleteAllNotes() {
        viewModelScope.launch { dao.deleteAllNotes() }
    }

    private fun getAllNotes() = dao.getAllNotes() // Private, cause is used only by mediator live data of view model

    private fun allNotesSortedFiltered( // Filter and sort notes for ui
        allNotes: MutableList<Note>,
        sortOrder: Int,
        sortChecked: Boolean,
        filterOrder: Int,
        filterChecked: Boolean,
        threshold: Int
    ): List<Note> {
        Notes.notes = allNotes // Input data to Notes object
        Notes.sortOrder = sortOrder
        Notes.sortChecked = sortChecked
        Notes.filterOrder = filterOrder
        Notes.filterChecked = filterChecked
        Notes.threshold = threshold

        Notes.sortAndFilter() // Notes object processes provided data

        return Notes.filteredSortedNotes ?: listOf<Note>()
    }

    fun prepopulate(notes: List<Note>) { // Recover previous state of ui
        val numberOfPagesNeeded = (notes.size.toDouble() / 6).roundToNextInt() // How many pages are needed
        if (numberOfPagesNeeded > 0) { // If pages needed are 0, no need to create pages
            val notesToBeInflatedChunked = notes.chunked(6) // Divide notes by 6 items parts
            for (page in 1..numberOfPagesNeeded) {
                val notesToBeInflated = notesToBeInflatedChunked[page - 1] // Notes to provide to page constructor
                createPage(notesToBeInflated) // When page is created it knows what notes to inflate
            }
            firstLaunch = false // From now prepopulation will not be called
        }
    }

    fun createPage(notesToBeInflated: List<Note>) {
        pages.add(PageFragment(notesToBeInflated))
        viewModelScope.launch {
            eventChannel.send(Event.SyncPagesId)
            eventChannel.send(Event.SetLastPage)
        }
    }

    fun cleanAndInflateAgain(notes: List<Note>) { // Erase all notes from screen and inflate updated data
        viewModelScope.launch { // Launch in coroutine, cause heavy-weight operation

            for (page in pages) {
                page.removeAllNotes() // First of all erase all notes from all pages
            }

            val numberOfPagesNeeded = (notes.size.toDouble() / 6).roundToNextInt()
            val notesToBeInflatedChunked = notes.chunked(6)
            var numberOfChunkedInterval = 0

            when { // Comparison if new data set requires more or less pages, than were created previously
                numberOfPagesNeeded == pages.size -> { // No need to delete or add more pages
                    for (page in pages) {
                        val notesToBeInflated =
                            notesToBeInflatedChunked[numberOfChunkedInterval]
                        page.inflateNotes(notesToBeInflated)
                        numberOfChunkedInterval++
                    }
                }
                numberOfPagesNeeded > pages.size -> { // Some notes are inflated on existing pages
                    for (page in pages) {
                        val notesToBeInflated =
                            notesToBeInflatedChunked[numberOfChunkedInterval]
                        page.inflateNotes(notesToBeInflated)
                        numberOfChunkedInterval++
                    }
                    for (page in 1..(numberOfPagesNeeded - pages.size)) { // The rest of notes require new pages
                        val notesToBeInflated =
                            notesToBeInflatedChunked[numberOfChunkedInterval]
                        createPage(notesToBeInflated)
                        numberOfChunkedInterval++
                    }
                }
                numberOfPagesNeeded < pages.size -> { // Some notes are inflated on existing pages
                    for (page in 1..numberOfPagesNeeded) {
                        val notesToBeInflated =
                            notesToBeInflatedChunked[numberOfChunkedInterval]
                        pages[page - 1].inflateNotes(notesToBeInflated)
                        numberOfChunkedInterval++
                    }
                    for (page in pages.lastIndex downTo numberOfPagesNeeded) { // Empty pages has to be deleted
                        pages.removeAt(page)
                    }
                    viewModelScope.launch {
                        eventChannel.send(Event.SyncPagesId)
                        if (pages.lastIndex >= currentPage) eventChannel.send(Event.SetCurrentPage)
                        else eventChannel.send(Event.SetLastPage)
                    }
                }
            }
        }
    }

    fun changeSortOrder() {
        if (sortChecked) {
            when (sortOrder) {
                SortOrder.ASC -> sortOrderLd.value = SortOrder.DSC // Fire live data with opposite value
                SortOrder.DSC -> sortOrderLd.value = SortOrder.ASC
            }
        } else
            when (sortOrder) {
                SortOrder.ASC -> sortOrder = SortOrder.DSC // Change property to opposite value
                SortOrder.DSC -> sortOrder = SortOrder.ASC
            }
    }

    fun changeSortState() {
        sortCheckedLd.value = !sortChecked
    }

    fun changeFilterOrder() {
        if (filterChecked) {
            when (filterOrder) {
                FilterOrder.MORE -> filterOrderLd.value = FilterOrder.LESS // Fire live data with opposite value
                FilterOrder.LESS -> filterOrderLd.value = FilterOrder.MORE
            }
        } else
            when (filterOrder) {
                FilterOrder.MORE -> filterOrder = FilterOrder.LESS // Change property to opposite value
                FilterOrder.LESS -> filterOrder = FilterOrder.MORE
            }
    }

    fun changeFilterState() {
        filterCheckedLd.value = !filterChecked
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun selectImage(mood: Int): Drawable { // Pick the picture based on mood
        return when {
            mood <= 3 -> resources.getDrawable(R.drawable.emoji_1_3)
            mood <= 6 -> resources.getDrawable(R.drawable.emoji_4_6)
            mood <= 9 -> resources.getDrawable(R.drawable.emoji_7_9)
            else -> resources.getDrawable(R.drawable.emoji_10)
        }
    }
}

