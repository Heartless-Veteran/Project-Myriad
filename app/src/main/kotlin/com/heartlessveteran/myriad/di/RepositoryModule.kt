package com.heartlessveteran.myriad.di

import com.heartlessveteran.myriad.core.data.repository.AnimeRepositoryImpl
import com.heartlessveteran.myriad.core.data.repository.MangaRepositoryImpl
import com.heartlessveteran.myriad.core.data.repository.PluginRepositoryImpl
import com.heartlessveteran.myriad.core.data.repository.ReaderSettingsRepositoryImpl
import com.heartlessveteran.myriad.core.domain.repository.AnimeRepository
import com.heartlessveteran.myriad.core.domain.repository.MangaRepository
import com.heartlessveteran.myriad.core.domain.repository.PluginRepository
import com.heartlessveteran.myriad.core.domain.repository.ReaderSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository bindings.
 * Binds repository implementations to their interfaces.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMangaRepository(mangaRepositoryImpl: MangaRepositoryImpl): MangaRepository

    @Binds
    @Singleton
    abstract fun bindAnimeRepository(animeRepositoryImpl: AnimeRepositoryImpl): AnimeRepository

    @Binds
    @Singleton
    abstract fun bindPluginRepository(pluginRepositoryImpl: PluginRepositoryImpl): PluginRepository

    @Binds
    @Singleton
    abstract fun bindReaderSettingsRepository(readerSettingsRepositoryImpl: ReaderSettingsRepositoryImpl): ReaderSettingsRepository
}
