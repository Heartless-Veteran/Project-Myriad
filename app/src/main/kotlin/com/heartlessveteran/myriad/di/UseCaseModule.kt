package com.heartlessveteran.myriad.di

import com.heartlessveteran.myriad.core.domain.repository.MangaRepository
import com.heartlessveteran.myriad.core.domain.usecase.AddMangaToLibraryUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetChapterPagesUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetLibraryMangaUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetMangaDetailsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module that provides use case instances.
 * Use cases encapsulate business logic and coordinate between UI and repository layers.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    /**
     * Provides GetLibraryMangaUseCase.
     * @param mangaRepository Repository for manga data operations
     * @return GetLibraryMangaUseCase instance
     */
    @Provides
    fun provideGetLibraryMangaUseCase(
        mangaRepository: MangaRepository
    ): GetLibraryMangaUseCase {
        return GetLibraryMangaUseCase(mangaRepository)
    }

    /**
     * Provides GetMangaDetailsUseCase.
     * @param mangaRepository Repository for manga data operations
     * @return GetMangaDetailsUseCase instance
     */
    @Provides
    fun provideGetMangaDetailsUseCase(
        mangaRepository: MangaRepository
    ): GetMangaDetailsUseCase {
        return GetMangaDetailsUseCase(mangaRepository)
    }

    /**
     * Provides AddMangaToLibraryUseCase.
     * @param mangaRepository Repository for manga data operations
     * @return AddMangaToLibraryUseCase instance
     */
    @Provides
    fun provideAddMangaToLibraryUseCase(
        mangaRepository: MangaRepository
    ): AddMangaToLibraryUseCase {
        return AddMangaToLibraryUseCase(mangaRepository)
    }

    /**
     * Provides GetChapterPagesUseCase.
     * @param mangaRepository Repository for manga data operations
     * @return GetChapterPagesUseCase instance
     */
    @Provides
    fun provideGetChapterPagesUseCase(
        mangaRepository: MangaRepository
    ): GetChapterPagesUseCase {
        return GetChapterPagesUseCase(mangaRepository)
    }
}