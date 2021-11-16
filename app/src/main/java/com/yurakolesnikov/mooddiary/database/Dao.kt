package com.yurakolesnikov.mooddiary.database

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao
import com.yurakolesnikov.mooddiary.database.model.Note

@Dao
interface Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateNote (note: Note)

    @Delete
    suspend fun  deleteNote (note: Note)

    @Query ("DELETE FROM Note")
    suspend fun deleteAllNotes ()

    @Query ("SELECT * FROM Note")
    fun getAllNotes () : LiveData<List<Note>>
}