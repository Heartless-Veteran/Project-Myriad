package com.heartlessveteran.myriad.di

import com.heartlessveteran.myriad.core.domain.usecase.GetChapterPagesUseCase
import com.heartlessveteran.myriad.core.domain.repository.Source
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for use case dependencies.
 * Provides use case instances with their required dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetChapterPagesUseCase(
        sources: Map<String, @JvmSuppressWildcards Source>
    ): GetChapterPagesUseCase = GetChapterPagesUseCase(sources)

    @Provides
    @Singleton
    fun provideSources(): Map<String, Source> {
        // TODO: Initialize actual sources when implemented
        return emptyMap()
    }
}