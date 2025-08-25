package com.projectmyriad.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.projectmyriad.data.database.dao.AnimeDao
import com.projectmyriad.data.database.dao.MangaDao
import com.projectmyriad.domain.entities.Anime
import com.projectmyriad.domain.entities.AnimeEpisode
import com.projectmyriad.domain.entities.Manga
import com.projectmyriad.domain.entities.MangaChapter

/**
 * Main Room database for Project Myriad
 * Contains all entities and provides access to DAOs
 */
@Database(
    entities = [
        Manga::class,
        MangaChapter::class,
        Anime::class,
        AnimeEpisode::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MyriadDatabase : RoomDatabase() {
    
    abstract fun mangaDao(): MangaDao
    abstract fun animeDao(): AnimeDao
    
    companion object {
        const val DATABASE_NAME = "myriad_database"
        
        @Volatile
        private var INSTANCE: MyriadDatabase? = null
        
        fun getDatabase(context: Context): MyriadDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyriadDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}