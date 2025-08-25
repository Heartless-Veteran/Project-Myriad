package com.projectmyriad.di

import android.content.Context
import androidx.room.Room
import com.projectmyriad.data.database.ProjectMyriadDatabase
import com.projectmyriad.data.database.dao.MangaDao
import com.projectmyriad.data.repositories.MangaRepositoryImpl
import com.projectmyriad.domain.repositories.MangaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module for database and repository dependencies.
 * Provides singleton instances of database, DAOs, and repositories.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideProjectMyriadDatabase(
        @ApplicationContext context: Context
    ): ProjectMyriadDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ProjectMyriadDatabase::class.java,
            "project_myriad_database"
        )
        .fallbackToDestructiveMigration() // Remove in production
        .build()
    }
    
    @Provides
    fun provideMangaDao(database: ProjectMyriadDatabase): MangaDao {
        return database.mangaDao()
    }
    
    @Provides
    @Singleton
    fun provideMangaRepository(mangaDao: MangaDao): MangaRepository {
        return MangaRepositoryImpl(mangaDao)
    }
}