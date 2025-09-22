package com.heartlessveteran.myriad.di

import android.content.Context
import androidx.room.Room
import com.heartlessveteran.myriad.core.data.database.ChapterDao
import com.heartlessveteran.myriad.core.data.database.DatabaseConverters
import com.heartlessveteran.myriad.core.data.database.MangaDao
import com.heartlessveteran.myriad.core.data.database.MyriadDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides database-related dependencies.
 * Provides singleton instances of database and DAOs for the entire application.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the main application database.
     * @param context Application context
     * @return Configured database instance
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MyriadDatabase {
        return Room.databaseBuilder(
            context,
            MyriadDatabase::class.java,
            MyriadDatabase.DATABASE_NAME
        )
            .addTypeConverter(DatabaseConverters())
            .build()
    }

    /**
     * Provides MangaDao from the database.
     * @param database Application database
     * @return MangaDao instance
     */
    @Provides
    fun provideMangaDao(database: MyriadDatabase): MangaDao = database.mangaDao()

    /**
     * Provides ChapterDao from the database.
     * @param database Application database
     * @return ChapterDao instance
     */
    @Provides
    fun provideChapterDao(database: MyriadDatabase): ChapterDao = database.chapterDao()
}