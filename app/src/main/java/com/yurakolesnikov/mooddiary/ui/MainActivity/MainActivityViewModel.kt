package com.yurakolesnikov.mooddiary.ui.MainActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurakolesnikov.mooddiary.database.Dao
import com.yurakolesnikov.mooddiary.database.model.Note
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    val dao: Dao
) : ViewModel() {

    fun insertNote(note: Note) {
        viewModelScope.launch { dao.insertNote(note) }
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
}