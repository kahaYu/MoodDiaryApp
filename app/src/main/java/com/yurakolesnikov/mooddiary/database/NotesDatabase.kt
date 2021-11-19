package com.yurakolesnikov.mooddiary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yurakolesnikov.mooddiary.database.model.Note
import com.yurakolesnikov.mooddiary.utils.Converters

@Database(entities = [Note::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NotesDatabase :RoomDatabase() {

    abstract fun getDao (): Dao
}