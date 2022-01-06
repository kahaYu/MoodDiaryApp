package com.yurakolesnikov.mooddiary.ui.mainActivity

import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import androidx.lifecycle.*
import com.yurakolesnikov.mooddiary.database.Dao
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.databinding.ItemViewBinding
import com.yurakolesnikov.mooddiary.ui.PageFragment
import com.yurakolesnikov.mooddiary.utils.FilterOrder
import com.yurakolesnikov.mooddiary.utils.Notes
import com.yurakolesnikov.mooddiary.utils.SortOrder
import com.yurakolesnikov.mooddiary.utils.roundToNextInt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val dao: Dao,
    private val sp: SharedPreferences,
    val spEditor: SharedPreferences.Editor
) : ViewModel() {

    var firstLaunch = true
    var isAlwaysYes = sp.getBoolean("isAlwaysYes", false)
    var isAlwaysNo = sp.getBoolean("isAlwaysNo", false)

    var sortOrderLD = MutableLiveData<Int>()
    var sortOrder = SortOrder.ASC
    var sortCheckedLD = MutableLiveData<Boolean>(false)
    var sortChecked = false

    var filterOrderLD = MutableLiveData<Int>()
    var filterOrder = FilterOrder.MORE
    var filterCheckedLD = MutableLiveData<Boolean>(false)
    var filterChecked = false

    var thresholdLD = MutableLiveData<Int>()
    var threshold = 1

    val pages = mutableListOf<PageFragment>()

    var notesNoLiveData = mutableListOf<Note>()

    val allNotesSortedFiltered = object : MediatorLiveData<List<Note>>() {
        init {
            addSource(getAllNotes()) { notes ->
                notesNoLiveData = notes as MutableList<Note>
                getAllNotesSortedFiltered()
            }
            addSource(sortOrderLD) { sortOrderValue ->
                sortOrder = sortOrderValue
                getAllNotesSortedFiltered()
            }
            addSource(sortCheckedLD) { sortCheckedValue ->
                sortChecked = sortCheckedValue
                getAllNotesSortedFiltered()
            }
            addSource(filterOrderLD) { filterOrderValue ->
                filterOrder = filterOrderValue
                getAllNotesSortedFiltered()
            }
            addSource(filterCheckedLD) { filterCheckedValue ->
                filterChecked = filterCheckedValue
                getAllNotesSortedFiltered()
            }
            addSource(thresholdLD) { thresholdValue ->
                threshold = thresholdValue
                getAllNotesSortedFiltered()
            }
        }

        private fun getAllNotesSortedFiltered() {
            viewModelScope.launch {
                value = allNotesSortedFiltered(
                    notesNoLiveData, sortOrder, sortChecked, filterOrder, filterChecked, threshold
                )
            }
        }
    }


    fun insertNote(note: Note) {
        viewModelScope.launch {
            if (notesNoLiveData.size == 18) deleteFirstSixNotes().also { eventChannel.send(Event
                .showToastNotesLimit()) }
            dao.insertNote(note)
            eventChannel.send(Event.setLastPage())
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch { dao.updateNote(note) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            dao.deleteNote(note)
            eventChannel.send(Event.showUndoDeleteionSnackbar(note))
        }
    }

    fun deleteAllNotes() {
        viewModelScope.launch { dao.deleteAllNotes() }
    }

    fun getAllNotes() = dao.getAllNotes()

    fun allNotesSortedFiltered(
        allNotes: MutableList<Note>,
        sortOrder: Int,
        sortChecked: Boolean,
        filterOrder: Int,
        filterChecked: Boolean,
        threshold: Int
    ): List<Note> {
        Notes.notes = allNotes
        Notes.sortOrder = sortOrder
        Notes.sortChecked = sortChecked
        Notes.filterOrder = filterOrder
        Notes.filterChecked = filterChecked
        Notes.threshold = threshold

        Notes.sortAndFilter()

        return Notes.filteredSortedNotes!!
    }

    fun deleteFirstSixNotes() {
        viewModelScope.launch { dao.deleteFirstSixNotes() }
    }

    fun setPreviewImage(image: Drawable) {
        previewImage.value = image
    }

    fun removeAllNotesFromScreens() {
        for (page in pages) {
            page.removeAllNotes()
        }
    }

    fun undoDeleteNote(note: Note) {
        viewModelScope.launch {
            dao.insertNote(note)
        }
    }

    fun prepopulate(notes: List<Note>) {
        val numberOfPagesNeeded = (notes.size.toDouble() / 6).roundToNextInt()
        if (notes.size > 0) MainActivity.NOTE_ID = notesNoLiveData.last().id + 1
        if (numberOfPagesNeeded > 0) { // If pages is 0, no need to create.
            val notesToBeInflatedChunked = notes.chunked(6) // Divide by 6 items parts.
            for (page in 1..numberOfPagesNeeded) {
                val notesToBeInflated = notesToBeInflatedChunked[page - 1]
                createPage(notesToBeInflated) // When page is created it knows what to inflate.
            }
            firstLaunch = false
        }
    }

    fun createPage(notesToBeInflated: List<Note>) {
        pages.add(PageFragment(notesToBeInflated))
        viewModelScope.launch {
            eventChannel.send(Event.syncPagesId())
            eventChannel.send(Event.setLastPage())
        }
    }

    fun deleteAllPages() {
        pages.clear()
    }

    fun cleanAndInflateAgain(notes: List<Note>) {
        viewModelScope.launch {
            for (page in pages) {
                page.removeAllNotes()
            }
            val numberOfPagesNeeded = (notes.size.toDouble() / 6).roundToNextInt()
            val notesToBeInflatedChunked = notes.chunked(6)
            var numberOfChunkedInterval = 0

            when {
                numberOfPagesNeeded == pages.size -> {
                    for (page in pages) {
                        val notesToBeInflated =
                            notesToBeInflatedChunked[numberOfChunkedInterval]
                        page.inflateNotes(notesToBeInflated)
                        numberOfChunkedInterval++
                    }
                }
                numberOfPagesNeeded > pages.size -> {
                    for (page in pages) {
                        val notesToBeInflated =
                            notesToBeInflatedChunked[numberOfChunkedInterval]
                        page.inflateNotes(notesToBeInflated)
                        numberOfChunkedInterval++
                    }
                    for (page in 1..(numberOfPagesNeeded - pages.size)) {
                        val notesToBeInflated =
                            notesToBeInflatedChunked[numberOfChunkedInterval]
                        createPage(notesToBeInflated)
                        numberOfChunkedInterval++
                    }
                }
                numberOfPagesNeeded < pages.size -> {
                    for (page in 1..numberOfPagesNeeded) {
                        val notesToBeInflated =
                            notesToBeInflatedChunked[numberOfChunkedInterval]
                        pages[page - 1].inflateNotes(notesToBeInflated)
                        numberOfChunkedInterval++
                    }
                    for (page in pages.lastIndex downTo numberOfPagesNeeded) {
                        pages.removeAt(page)
                    }
                    viewModelScope.launch {
                        eventChannel.send(Event.syncPagesId())
                        eventChannel.send(Event.setLastPage())
                    }
                }
            }
        }
    }

    fun changeSortOrder() {
        if (sortChecked) {
            when (sortOrder) {
                SortOrder.ASC -> sortOrderLD.value = SortOrder.DSC
                SortOrder.DSC -> sortOrderLD.value = SortOrder.ASC
            }
        } else
            if (sortOrder == SortOrder.ASC) sortOrder = SortOrder.DSC
            else sortOrder = SortOrder.ASC
    }

    fun changeSortState() {
        sortCheckedLD.value = !sortChecked
    }

    fun changeFilterOrder() {
        if (filterChecked) {
            when (filterOrder) {
                FilterOrder.MORE -> filterOrderLD.value = FilterOrder.LESS
                FilterOrder.LESS -> filterOrderLD.value = FilterOrder.MORE
            }
        } else
            if (filterOrder == FilterOrder.MORE) filterOrder = FilterOrder.LESS
            else filterOrder = FilterOrder.MORE
    }

    fun changeFilterState() {
        filterCheckedLD.value = !filterChecked
    }

    var currentPage: Int? = null

    var itemViewBinding: ItemViewBinding? = null

    var previewImage = MutableLiveData<Drawable>()
    var isVisible = MutableLiveData<Boolean>()

    var isChecked = false


    var isNoteDeletion = false
    var isNoteInsert = false
    var isNoteUpdate = false
    var isUndoDeletion = false
    var pageFromWhereTapped = 0


    private val eventChannel = Channel<Event>()
    val event = eventChannel.receiveAsFlow()

    sealed class Event {
        data class showUndoDeleteionSnackbar(val note: Note) : Event()
        class syncPagesId : Event()
        class setLastPage : Event()
        class showToastNotesLimit : Event()
    }
}

