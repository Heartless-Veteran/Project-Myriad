package com.heartlessveteran.myriad.di

import com.heartlessveteran.myriad.data.cache.MemoryCache
import com.heartlessveteran.myriad.data.services.OCRService
import com.heartlessveteran.myriad.domain.usecase.GetRecommendationsUseCase
import com.heartlessveteran.myriad.network.GeminiService
import com.heartlessveteran.myriad.services.EnhancedAIService
import com.heartlessveteran.myriad.services.SmartCacheService

/**
 * Manual dependency injection container for AI Core features.
 * Temporary solution until Hilt/KSP is fully enabled.
 *
 * This container provides:
 * - OCRService for text recognition and translation
 * - GetRecommendationsUseCase for AI recommendations
 * - EnhancedAIService for coordinated AI operations
 * - Caching services for AI results
 */
object AIDiContainer {
    @Volatile
    private var ocrService: OCRService? = null

    @Volatile
    private var enhancedAIService: EnhancedAIService? = null

    @Volatile
    private var smartCacheService: SmartCacheService? = null

    @Volatile
    private var recommendationsUseCase: GetRecommendationsUseCase? = null

    /**
     * Get OCRService instance.
     */
    fun getOCRService(): OCRService =
        ocrService ?: synchronized(this) {
            ocrService ?: OCRService().also { ocrService = it }
        }

    /**
     * Get SmartCacheService instance.
     */
    fun getSmartCacheService(): SmartCacheService =
        smartCacheService ?: synchronized(this) {
            smartCacheService ?: SmartCacheService(MemoryCache()).also { smartCacheService = it }
        }

    /**
     * Get GetRecommendationsUseCase instance.
     */

    /**
     * Get GetRecommendationsUseCase with context.
     */
    fun getRecommendationsUseCase(context: android.content.Context): GetRecommendationsUseCase =
        recommendationsUseCase ?: synchronized(this) {
            recommendationsUseCase ?: GetRecommendationsUseCase(
                mangaRepository = LibraryDiContainer.getMangaRepository(context),
            ).also { recommendationsUseCase = it }
        }

    /**
     * Get GeminiService instance with manual construction.
     */
    private fun createGeminiService(): GeminiService {
        // Create basic implementation since we're using manual DI
        // The actual service will be created via the existing network infrastructure
        return NetworkModule.provideGeminiService(
            NetworkModule.provideGeminiRetrofit(
                NetworkModule.provideGeminiOkHttpClient(
                    NetworkModule.provideHttpLoggingInterceptor(),
                    NetworkModule.provideGeminiAuthInterceptor(),
                ),
                NetworkModule.provideJson(),
            ),
        )
    }

    /**
     * Get EnhancedAIService instance with all dependencies.
     */
    fun getEnhancedAIService(context: android.content.Context): EnhancedAIService =
        enhancedAIService ?: synchronized(this) {
            enhancedAIService ?: EnhancedAIService(
                geminiService = createGeminiService(),
                cacheService = getSmartCacheService(),
                ocrService = getOCRService(),
                recommendationsUseCase = getRecommendationsUseCase(context),
            ).also { enhancedAIService = it }
        }

    /**
     * Clear all cached instances (useful for testing).
     */
    fun clearInstances() {
        synchronized(this) {
            ocrService?.cleanup()
            ocrService = null
            enhancedAIService = null
            smartCacheService = null
            recommendationsUseCase = null
        }
    }
}
