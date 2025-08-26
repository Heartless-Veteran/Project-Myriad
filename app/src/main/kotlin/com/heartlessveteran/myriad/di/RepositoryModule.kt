package com.heartlessveteran.myriad.di

import com.heartlessveteran.myriad.data.repository.MangaRepositoryImpl
import com.heartlessveteran.myriad.domain.repository.MangaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding repository implementations
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindMangaRepository(
        mangaRepositoryImpl: MangaRepositoryImpl
    ): MangaRepository
    
    // TODO: Add AnimeRepository binding when implemented
    // TODO: Add AIRepository binding when implemented
    // TODO: Add UserRepository binding when implemented
}