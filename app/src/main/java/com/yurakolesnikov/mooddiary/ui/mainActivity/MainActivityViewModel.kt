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
        updateNoteTrigger.value = note
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { dao.deleteNote(note) }
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


    var insertNoteTrigger = MutableLiveData<Note>()
    var updateNoteTrigger = MutableLiveData<Note>()
    var createPageTrigger = MutableLiveData<Note>()
    var deleteAllNotesTrigger = MutableLiveData<Boolean>()
    var sortTrigger = MutableLiveData<Boolean>()
    var sortTriggerNoLiveData = false


    var itemViewBinding: ItemViewBinding? = null
    var pageFromWhereTapped: Int? = null

    var previewImage = MutableLiveData<Drawable>()
    var isVisible = MutableLiveData<Boolean>()

    var isChecked = false
    var isAlwaysYes = false
    var isAlwaysNo = false

}