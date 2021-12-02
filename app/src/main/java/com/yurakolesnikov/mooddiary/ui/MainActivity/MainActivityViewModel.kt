package com.yurakolesnikov.mooddiary.ui.MainActivity

import android.graphics.drawable.Drawable
import android.widget.FrameLayout
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

    fun setPreviewImage(image: Drawable) {
        previewImage.value = image
    }

    var insertNoteTrigger = MutableLiveData<Note>()
    var updateNoteTrigger = MutableLiveData<Note>()
    var createPageTrigger = MutableLiveData<Note>()
    var deleteAllNotesTrigger = MutableLiveData<Boolean>()

    var itemViewBinding: ItemViewBinding? = null
    var pageFromWhereTapped: Int? = null

    var previewImage = MutableLiveData<Drawable>()

}