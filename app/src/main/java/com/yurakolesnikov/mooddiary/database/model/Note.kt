package com.yurakolesnikov.mooddiary.database.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.yurakolesnikov.mooddiary.ui.MainActivity.MainActivity
import com.yurakolesnikov.mooddiary.utils.Converters
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@Entity
class Note(
    val date: String,
    var mood: Int,
    @PrimaryKey(autoGenerate = false)
    var id: Int = 0
) {

}