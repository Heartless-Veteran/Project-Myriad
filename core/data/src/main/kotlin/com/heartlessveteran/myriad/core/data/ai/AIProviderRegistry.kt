package com.heartlessveteran.myriad.core.data.ai

import com.heartlessveteran.myriad.core.domain.ai.AIProvider

/**
 * Registry for managing available AI providers.
 * Provides centralized access to all AI providers in the application.
 * Uses manual dependency injection pattern following the project's current architecture.
 */
class AIProviderRegistry(
    private val geminiProvider: GeminiProvider,
    private val openAIProvider: OpenAIProvider
) {
    private val providers: List<AIProvider> = listOf(geminiProvider, openAIProvider)

    /**
     * Get all available AI providers
     */
    fun getProviders(): List<AIProvider> = providers.filter { it.isAvailable }

    /**
     * Get a specific provider by name
     */
    fun getProviderByName(name: String): AIProvider? =
        providers.find { it.name == name && it.isAvailable }

    /**
     * Get the default provider (first available provider)
     */
    fun getDefaultProvider(): AIProvider =
        providers.firstOrNull { it.isAvailable }
            ?: throw IllegalStateException("No AI providers available")

    /**
     * Check if any providers are available
     */
    fun hasAvailableProviders(): Boolean = providers.any { it.isAvailable }
}