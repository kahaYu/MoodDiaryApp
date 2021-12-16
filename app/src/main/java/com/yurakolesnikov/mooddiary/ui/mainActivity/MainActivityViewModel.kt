package com.yurakolesnikov.mooddiary.ui.mainActivity

import android.graphics.drawable.Drawable
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurakolesnikov.mooddiary.database.Dao
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.databinding.ItemViewBinding
import com.yurakolesnikov.mooddiary.ui.PageFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val dao: Dao
) : ViewModel() {

    val pages = mutableListOf<PageFragment>()

    var isFirstLaunch = true

    fun insertNote(note: Note) {
        viewModelScope.launch { dao.insertNote(note) }
        isNoteInsert = true
        insertNoteTrigger.value = note

    }

    fun updateNote(note: Note) {
        viewModelScope.launch { dao.updateNote(note) }
        isNoteUpdate = true
        updateNoteTrigger.value = note
    }

    fun deleteNote(note: Note) {
        deletedNote = note
        viewModelScope.launch {
            dao.deleteNote(note)
            eventChannel.send(Event.showUndoDeleteionSnackbar(note))
        }
        isNoteDeletion = true
        pages.clear()
        syncPagesIdTrigger.value = true

    }

    fun deleteAllNotes() {
        viewModelScope.launch { dao.deleteAllNotes() }

        pages.clear()
        deleteAllNotesTrigger.value = true

    }

    fun getAllNotes() = dao.getAllNotes()

    fun deleteFirstSixNotes() {
        viewModelScope.launch { dao.deleteFirstSixNotes() }
    }

    fun setPreviewImage(image: Drawable) {
        previewImage.value = image
    }

    fun sortTrigger() {
        sortTrigger.value = !sortTriggerNoLiveData
        sortTriggerNoLiveData = !sortTriggerNoLiveData
    }

    fun changeOrder() {
        sortOrder = if (sortOrder == ASC) DSC else ASC
        if (sortTriggerNoLiveData) {
            sortTrigger.value = true
        }
    }

    fun removeAllNotesFromScreens() {
        for (page in pages) {
            page.removeAllNotes()
        }
    }

    fun undoDeleteNote(note: Note) {
        isUndoDeletion = true
        pages.clear()
        syncPagesIdTrigger.value = true
        viewModelScope.launch {
            dao.insertNote(note)
        }
    }


    var insertNoteTrigger = MutableLiveData<Note>()
    var updateNoteTrigger = MutableLiveData<Note>()
    var createPageTrigger = MutableLiveData<Note>()
    var deleteAllNotesTrigger = MutableLiveData<Boolean>()
    var sortTrigger = MutableLiveData<Boolean>()
    var syncPagesIdTrigger = MutableLiveData<Boolean>()
    var undoTrigger = MutableLiveData<Boolean>()
    var sortTriggerNoLiveData = false
    var sortOrder = ASC
    var filterOrder = MORE
    var currentPage: Int? = null

    var deletedNote: Note? = null


    var itemViewBinding: ItemViewBinding? = null
    var pageFromWhereTapped: Int? = null

    var previewImage = MutableLiveData<Drawable>()
    var isVisible = MutableLiveData<Boolean>()

    var isChecked = false
    var isAlwaysYes = false
    var isAlwaysNo = false
    var isNoteDeletion = false
    var isNoteInsert = false
    var isNoteUpdate = false
    var isUndoDeletion = false

    private val eventChannel = Channel<Event>()
    val event = eventChannel.receiveAsFlow()

    companion object {
        var ASC = 1
        var DSC = -1

        var MORE = 1
        var LESS = 1
    }

    sealed class Event {
        data class showUndoDeleteionSnackbar(val note: Note) : Event()
    }

}