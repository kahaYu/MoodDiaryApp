package com.yurakolesnikov.mooddiary.utils

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.yurakolesnikov.mooddiary.database.model.Note
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

object Notes { // Object processes filtering and sorting of data
    var notes: List<Note>? = null
        set(value) {
            field = value
            filteredSortedNotes = value // Just for naming
        }
    var filteredSortedNotes = notes
    var sortOrder = SortOrder.ASC
    var sortChecked = false
    var filterOrder = FilterOrder.MORE
    var filterChecked = false
    var threshold = 1

    fun sortAndFilter() {
        if (filterChecked) {
            when (filterOrder) {
                FilterOrder.MORE ->
                    filteredSortedNotes =
                        filteredSortedNotes?.filter { note -> note.mood >= threshold }
                FilterOrder.LESS ->
                    filteredSortedNotes =
                        filteredSortedNotes?.filter { note -> note.mood <= threshold }
            }
        }

        if (sortChecked) {
            when (sortOrder) {
                SortOrder.ASC -> // Source data is already filtered
                    filteredSortedNotes = filteredSortedNotes?.sortedBy { note -> note.mood }
                SortOrder.DSC ->
                    filteredSortedNotes =
                        filteredSortedNotes?.sortedByDescending { note -> note.mood }
            }
        }
    }
}

object SortOrder {
    const val ASC = 1
    const val DSC = -1
}

object FilterOrder {
    const val MORE = 1
    const val LESS = -1
}