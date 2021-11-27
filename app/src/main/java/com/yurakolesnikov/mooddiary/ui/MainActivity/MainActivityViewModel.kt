package com.yurakolesnikov.mooddiary.ui.MainActivity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurakolesnikov.mooddiary.database.Dao
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.ui.PageFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val dao: Dao
) : ViewModel() {

    val pages = mutableListOf<PageFragment>()

    fun insertNote(note: Note) {
        viewModelScope.launch { dao.insertNote(note) }
        insertNoteTrigger.value = note
    }

    fun updateNote(note: Note) {
        viewModelScope.launch { dao.updateNote(note) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { dao.deleteNote(note) }
    }

    fun deleteAllNotes() {
        viewModelScope.launch { dao.deleteAllNotes() }
        pages.clear()

        insertNoteTrigger = MutableLiveData<Note>()
        deleteAllNotesTrigger.value = true

    }

    fun getAllNotes() = dao.getAllNotes()

    var insertNoteTrigger = MutableLiveData<Note>()
    var deleteAllNotesTrigger = MutableLiveData<Boolean>()
}