package com.heartlessveteran.myriad.di

import com.heartlessveteran.myriad.core.data.database.ChapterDao
import com.heartlessveteran.myriad.core.data.database.MangaDao
import com.heartlessveteran.myriad.core.data.repository.MangaRepositoryImpl
import com.heartlessveteran.myriad.core.domain.repository.MangaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides repository implementations.
 * Binds domain repository interfaces to their data layer implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provides MangaRepository implementation.
     * @param mangaDao Database access object for manga operations
     * @param chapterDao Database access object for chapter operations
     * @return MangaRepository implementation
     */
    @Provides
    @Singleton
    fun provideMangaRepository(
        mangaDao: MangaDao,
        chapterDao: ChapterDao
    ): MangaRepository {
        return MangaRepositoryImpl(mangaDao, chapterDao)
    }
}