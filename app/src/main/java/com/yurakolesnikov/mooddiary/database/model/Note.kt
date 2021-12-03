package com.yurakolesnikov.mooddiary.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Note(
    val date: String,
    var mood: Int,
    @PrimaryKey(autoGenerate = false)
    var id: Int = 0
) {

}