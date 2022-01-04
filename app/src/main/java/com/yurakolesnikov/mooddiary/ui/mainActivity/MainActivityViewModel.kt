package com.yurakolesnikov.mooddiary.ui.mainActivity

import android.graphics.drawable.Drawable
import androidx.lifecycle.*
import com.yurakolesnikov.mooddiary.database.Dao
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.databinding.ItemViewBinding
import com.yurakolesnikov.mooddiary.ui.PageFragment
import com.yurakolesnikov.mooddiary.utils.Notes
import com.yurakolesnikov.mooddiary.utils.roundToNextInt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val dao: Dao
) : ViewModel() {

    var firstLaunch = true

    var sortOrderLD = MutableLiveData(SortOrder.NONE)
    var filterOrderLD = MutableLiveData(FilterOrder.NONE)
    var thresholdLD = MutableLiveData(1)

    var sortOrder = SortOrder.NONE
    var filterOrder = FilterOrder.NONE
    var threshold = 1

    val pages = mutableListOf<PageFragment>()


    var notesNoLiveData = mutableListOf<Note>()

    val allNotesSortedFiltered = object : MediatorLiveData<List<Note>>() {
        init {
            addSource(getAllNotes()) { notes ->
                notesNoLiveData = notes as MutableList<Note>
                getAllNotesSortedFiltered() // No IF, cause this LD fires the last.
            }
            addSource(sortOrderLD) { sortOrderValue ->
                sortOrder = sortOrderValue
                if (!firstLaunch) getAllNotesSortedFiltered()
            }
            addSource(filterOrderLD) { filterOrderValue ->
                filterOrder = filterOrderValue
                if (!firstLaunch) getAllNotesSortedFiltered()
            }
            addSource(thresholdLD) { thresholdValue ->
                threshold = thresholdValue
                if (!firstLaunch) getAllNotesSortedFiltered()
            }
        }

        private fun getAllNotesSortedFiltered() {
            viewModelScope.launch {
                value = allNotesSortedFiltered(
                    notesNoLiveData, sortOrder, filterOrder, threshold
                )
            }
        }
    }


    fun insertNote(note: Note) {
        viewModelScope.launch { dao.insertNote(note) }
        isNoteInsert = true
    }

    fun updateNote(note: Note) {
        viewModelScope.launch { dao.updateNote(note) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { dao.deleteNote(note) }
    }

    fun deleteAllNotes() {
        viewModelScope.launch { dao.deleteAllNotes() }
    }

    fun getAllNotes() = dao.getAllNotes()

    fun allNotesSortedFiltered(
        allNotes: MutableList<Note>, sortOrder: Int, filterOrder: Int, threshold: Int
    ): List<Note> {
        Notes.notes = allNotes
        Notes.sortOrder = sortOrder
        Notes.filterOrder = filterOrder
        Notes.threshold = threshold

        Notes.sortAndFilter()

        return Notes.filteredSortedNotes!! as List<Note>
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
                    for (page in (pages.lastIndex + 1 - numberOfPagesNeeded)..pages.lastIndex) {
                        pages.removeAt(page)
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
                SortOrder.NONE -> sortOrderLD.value = SortOrder.ASC
            }
        } else sortOrderLD.value = SortOrder.NONE
    }

    fun changeSortChecked() {
        sortChecked = !sortChecked
        if (sortChecked) {
            when (sortOrder) {
                SortOrder.ASC -> sortOrderLD.value = SortOrder.ASC
                SortOrder.DSC -> sortOrderLD.value = SortOrder.DSC
                SortOrder.NONE -> sortOrderLD.value = SortOrder.ASC
            }
        } else sortOrderLD.value = SortOrder.NONE
    }

    fun changeFilterOrder() {
        if (filterChecked) {
            when (filterOrder) {
                FilterOrder.MORE -> filterOrderLD.value = FilterOrder.LESS
                FilterOrder.LESS -> sortOrderLD.value = FilterOrder.MORE
                FilterOrder.NONE -> sortOrderLD.value = FilterOrder.MORE
            }
        } else filterOrderLD.value = FilterOrder.NONE
    }

    fun changeFilterChecked() {
        filterChecked = !filterChecked
        if (filterChecked) {
            when (filterOrder) {
                FilterOrder.MORE -> filterOrderLD.value = FilterOrder.MORE
                FilterOrder.LESS -> sortOrderLD.value = FilterOrder.LESS
                FilterOrder.NONE -> sortOrderLD.value = FilterOrder.MORE
            }
        } else filterOrderLD.value = FilterOrder.NONE
    }

    var currentPage: Int? = null

    var itemViewBinding: ItemViewBinding? = null

    var previewImage = MutableLiveData<Drawable>()
    var isVisible = MutableLiveData<Boolean>()

    var isChecked = false
    var isAlwaysYes = false
    var isAlwaysNo = false
    var isNoteDeletion = false
    var isNoteInsert = false
    var isNoteUpdate = false
    var isUndoDeletion = false
    var pageFromWhereTapped = 0
    var sortChecked = false
    var filterChecked = false

    private val eventChannel = Channel<Event>()
    val event = eventChannel.receiveAsFlow()

    sealed class Event {
        data class showUndoDeleteionSnackbar(val note: Note) : Event()
        class syncPagesId : Event()
    }
}

object SortOrder {
    val ASC = 1
    val DSC = -1
    val NONE = 0
}

object FilterOrder {
    val MORE = 1
    val LESS = -1
    val NONE = 0
}