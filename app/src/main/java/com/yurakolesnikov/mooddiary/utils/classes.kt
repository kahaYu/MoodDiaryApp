package com.yurakolesnikov.mooddiary.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.ui.mainActivity.FilterOrder
import com.yurakolesnikov.mooddiary.ui.mainActivity.SortOrder
import java.util.logging.Filter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AutoClearedValue<T : Any>(val fragment: Fragment) : ReadWriteProperty<Fragment, T> {
    private var _value: T? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                    viewLifecycleOwner?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            _value = null
                        }
                    })
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return _value ?: throw IllegalStateException(
            "should never call auto-cleared-value get when it might not be available"
        )
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        _value = value
    }
}

object Notes {
    var notes: MutableList<Note>? = null
        set(value) {
            field = value
            filteredSortedNotes = value
        }
    var filteredSortedNotes = notes

    var sortOrder = SortOrder.NONE
    var filterOrder = FilterOrder.NONE
    var threshold = 1

    fun sortAndFilter() {
        if (filterOrder != FilterOrder.NONE) {
            when (filterOrder) {
                FilterOrder.MORE ->
                    filteredSortedNotes =
                        filteredSortedNotes?.filter { note -> note.mood >= threshold } as
                                MutableList<Note>?
                FilterOrder.LESS ->
                    filteredSortedNotes =
                        filteredSortedNotes?.filter { note -> note.mood <= threshold } as
                                MutableList<Note>?
            }
        }

        if (sortOrder != SortOrder.NONE) {
            when (sortOrder) {
                SortOrder.ASC ->
                    filteredSortedNotes = filteredSortedNotes?.sortedBy { note -> note.mood } as
                            MutableList<Note>?
                SortOrder.DSC ->
                    filteredSortedNotes =
                        filteredSortedNotes?.sortedByDescending { note -> note.mood } as
                                MutableList<Note>?
            }
        }
    }
}