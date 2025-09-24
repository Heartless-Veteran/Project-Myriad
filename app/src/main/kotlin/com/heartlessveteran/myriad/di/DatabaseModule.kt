package com.heartlessveteran.myriad.di

import android.content.Context
import androidx.room.Room
import com.heartlessveteran.myriad.core.data.database.ChapterDao
import com.heartlessveteran.myriad.core.data.database.MangaDao
import com.heartlessveteran.myriad.core.data.database.MyriadDatabase
import com.heartlessveteran.myriad.core.data.database.PluginDao
import com.heartlessveteran.myriad.core.data.database.ReaderSettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database-related dependencies.
 * Provides Room database and DAO instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): MyriadDatabase =
        Room
            .databaseBuilder(
                context.applicationContext,
                MyriadDatabase::class.java,
                MyriadDatabase.DATABASE_NAME,
            ).fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMangaDao(database: MyriadDatabase): MangaDao = database.mangaDao()

    @Provides
    fun provideChapterDao(database: MyriadDatabase): ChapterDao = database.chapterDao()

    @Provides
    fun providePluginDao(database: MyriadDatabase): PluginDao = database.pluginDao()

    @Provides
    fun provideReaderSettingsDao(database: MyriadDatabase): ReaderSettingsDao = database.readerSettingsDao()
}
