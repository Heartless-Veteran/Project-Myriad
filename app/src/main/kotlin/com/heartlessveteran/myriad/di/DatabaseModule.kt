package com.heartlessveteran.myriad.di

import android.content.Context
import androidx.room.Room
import com.heartlessveteran.myriad.data.database.MyriadDatabase
import com.heartlessveteran.myriad.data.database.dao.AnimeDao
import com.heartlessveteran.myriad.data.database.dao.MangaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MyriadDatabase {
        return Room.databaseBuilder(
            context,
            MyriadDatabase::class.java,
            MyriadDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideMangaDao(database: MyriadDatabase): MangaDao = database.mangaDao()
    
    @Provides
    fun provideAnimeDao(database: MyriadDatabase): AnimeDao = database.animeDao()
}