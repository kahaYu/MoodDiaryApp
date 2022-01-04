package com.yurakolesnikov.mooddiary.database

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.ui.mainActivity.FilterOrder
import com.yurakolesnikov.mooddiary.ui.mainActivity.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateNote (note: Note)

    @Delete
    suspend fun  deleteNote (note: Note)

    @Query("DELETE FROM Note")
    suspend fun deleteAllNotes ()

    @Query("DELETE FROM Note WHERE id IN (SELECT id FROM (SELECT ID FROM Note ORDER BY id ASC LIMIT 6))")
    suspend fun deleteFirstSixNotes()

    @Query ("SELECT * FROM Note")
    fun getAllNotes () : LiveData<List<Note>>

    //@Query ("SELECT * FROM Note ")
    //WHERE CASE WHEN :filterOrder = 1 THEN mood >= :threshold WHEN :filterOrder = -1 THEN mood <=
    //:threshold END ORDER BY CASE WHEN :sortOrder = 1 THEN mood END ASC, CASE WHEN :sortOrder =
    //-1 THEN mood END DESC
    //suspend fun getAllNotesSortedFiltered (
        //sortOrder: Int, filterOrder: Int, threshold: Int
    //) :
    //        List<Note>

}