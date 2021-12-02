package com.yurakolesnikov.mooddiary.di

import android.content.Context
import android.provider.ContactsContract
import androidx.databinding.adapters.Converters
import androidx.room.Room
import androidx.room.TypeConverters
import com.yurakolesnikov.mooddiary.database.Dao
import com.yurakolesnikov.mooddiary.database.NotesDatabase
import com.yurakolesnikov.mooddiary.ui.MainActivity.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

object AppModule {

    @Singleton
    @Provides
    fun provideDatabase (
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context, NotesDatabase::class.java, "NotesDatabase"
    ).build()

    @Singleton
    @Provides
    fun provideDao (database: NotesDatabase) = database.getDao()

}