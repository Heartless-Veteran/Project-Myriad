package com.heartlessveteran.myriad.core.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.entities.MangaChapter
import com.heartlessveteran.myriad.core.domain.entities.Plugin

/**
 * Room database for Project Myriad.
 * Central database that provides access to manga and chapter data.
 * Follows Clean Architecture principles and Room best practices.
 */
@Database(
    entities = [
        Manga::class,
        MangaChapter::class,
        Plugin::class,
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class MyriadDatabase : RoomDatabase() {

    /**
     * Provides access to manga data operations
     */
    abstract fun mangaDao(): MangaDao

    /**
     * Provides access to chapter data operations
     */
    abstract fun chapterDao(): ChapterDao

    /**
     * Provides access to plugin data operations
     */
    abstract fun pluginDao(): PluginDao

    companion object {
        /**
         * Database name for the application
         */
        const val DATABASE_NAME = "myriad_database"

        /**
         * Creates a database instance.
         * This method should be called from dependency injection framework.
         * @param context Application context
         * @return Configured database instance
         */
        fun create(context: Context): MyriadDatabase {
            return Room.databaseBuilder(
                context,
                MyriadDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }

        /**
         * Creates an in-memory database for testing purposes.
         * @param context Application context
         * @return In-memory database instance
         */
        fun createInMemory(context: Context): MyriadDatabase {
            return Room.inMemoryDatabaseBuilder(
                context,
                MyriadDatabase::class.java
            )
                .allowMainThreadQueries() // Only for testing
                .build()
        }
    }
}