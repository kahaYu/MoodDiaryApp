package com.yurakolesnikov.mooddiary.database.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.yurakolesnikov.mooddiary.utils.Converters

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val image: Bitmap,
    val date: String,
    val mood: Int
)