package com.yurakolesnikov.mooddiary.di

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.room.Room
import com.yurakolesnikov.mooddiary.database.NotesDatabase
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

    @Singleton
    @Provides
    fun providesSharedPreferences (
        @ApplicationContext context: Context
    ) = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)

    @Singleton
    @Provides
    fun providesEditor (sp: SharedPreferences) = sp.edit()

    @Singleton
    @Provides
    fun provideResources (
        @ApplicationContext context: Context
    ): Resources = context.resources

}