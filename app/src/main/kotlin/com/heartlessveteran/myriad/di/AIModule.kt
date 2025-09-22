package com.heartlessveteran.myriad.di

import com.heartlessveteran.myriad.core.data.ai.AIProviderRegistry
import com.heartlessveteran.myriad.core.data.ai.GeminiProvider
import com.heartlessveteran.myriad.core.data.ai.OpenAIProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for AI-related dependencies.
 * Provides AI providers and registry.
 */
@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideGeminiProvider(): GeminiProvider = GeminiProvider()

    @Provides
    @Singleton
    fun provideOpenAIProvider(): OpenAIProvider = OpenAIProvider()

    @Provides
    @Singleton
    fun provideAIProviderRegistry(
        geminiProvider: GeminiProvider,
        openAIProvider: OpenAIProvider
    ): AIProviderRegistry = AIProviderRegistry(geminiProvider, openAIProvider)
}