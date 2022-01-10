package com.yurakolesnikov.mooddiary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yurakolesnikov.mooddiary.database.model.Note

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NotesDatabase :RoomDatabase() { // No methods for db creation cause provide it with hilt

    abstract fun getDao (): Dao
}