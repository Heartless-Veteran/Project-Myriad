package com.projectmyriad.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.projectmyriad.data.database.dao.MangaDao
import com.projectmyriad.data.database.entities.MangaEntity
import com.projectmyriad.data.database.converters.Converters

/**
 * Project Myriad Room Database
 * Provides access to all DAOs and manages database schema.
 */
@Database(
    entities = [MangaEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ProjectMyriadDatabase : RoomDatabase() {
    
    abstract fun mangaDao(): MangaDao
    
    companion object {
        @Volatile
        private var INSTANCE: ProjectMyriadDatabase? = null
        
        private const val DATABASE_NAME = "project_myriad_database"
        
        fun getDatabase(context: Context): ProjectMyriadDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProjectMyriadDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration() // For development - remove in production
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}