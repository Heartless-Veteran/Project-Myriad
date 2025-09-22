package com.heartlessveteran.myriad.feature.ai.service

import com.heartlessveteran.myriad.core.data.ai.AIProviderRegistry
import com.heartlessveteran.myriad.core.domain.model.Result
import kotlinx.coroutines.*
import java.util.concurrent.Executors

/**
 * Enhanced AI Service that ensures all AI processing happens off the main thread.
 * Implements proper isolation for resource-intensive AI operations like OCR and translation.
 * Now uses the configurable AI provider system.
 */
class BackgroundAIProcessor(
    private val aiProviderRegistry: AIProviderRegistry
) {
    
    // Dedicated dispatcher for AI operations to avoid blocking main thread
    private val aiDispatcher = Executors.newFixedThreadPool(2) { thread ->
        Thread(thread, "AI-Worker").apply {
            isDaemon = true
            priority = Thread.NORM_PRIORITY - 1 // Lower priority than UI
        }
    }.asCoroutineDispatcher()
    
    // Scope for AI operations with custom dispatcher
    private val aiScope = CoroutineScope(aiDispatcher + SupervisorJob())
    
    /**
     * Process OCR translation in background with proper resource management
     * Now uses the selected AI provider from the registry
     */
    suspend fun processOCRTranslation(
        imageData: ByteArray,
        providerName: String? = null,
        onProgress: (Int) -> Unit = {},
        onComplete: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        aiScope.launch {
            try {
                onProgress(10)
                
                val provider = providerName?.let { 
                    aiProviderRegistry.getProviderByName(it) 
                } ?: aiProviderRegistry.getDefaultProvider()
                
                onProgress(50)
                
                val result = provider.translateText(imageData)
                
                onProgress(100)
                
                // Return result on main thread
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Result.Success -> onComplete(result.data)
                        is Result.Error -> onError(result.exception as? Exception ?: Exception("Wrapped throwable", result.exception))
                        is Result.Loading -> {} // Should not happen
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
    
    /**
     * Process art style matching in background
     * Now uses the selected AI provider from the registry
     */
    suspend fun processArtStyleMatching(
        imageData: ByteArray,
        providerName: String? = null,
        onComplete: (List<String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        aiScope.launch {
            try {
                val provider = providerName?.let { 
                    aiProviderRegistry.getProviderByName(it) 
                } ?: aiProviderRegistry.getDefaultProvider()
                
                val result = provider.analyzeArtStyle(imageData)
                
                // Return results on main thread
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Result.Success -> onComplete(result.data)
                        is Result.Error -> onError(Exception(result.exception))
                        is Result.Loading -> {} // Should not happen
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
    
    /**
     * Generate AI recommendations in background
     * Now uses the selected AI provider from the registry
     */
    suspend fun generateRecommendations(
        userPreferences: Map<String, Any>,
        providerName: String? = null,
        onComplete: (List<String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        aiScope.launch {
            try {
                val provider = providerName?.let { 
                    aiProviderRegistry.getProviderByName(it) 
                } ?: aiProviderRegistry.getDefaultProvider()
                
                val result = provider.generateRecommendations(userPreferences)
                
                // Return results on main thread
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Result.Success -> onComplete(result.data)
                        is Result.Error -> onError(Exception(result.exception))
                        is Result.Loading -> {} // Should not happen
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
    
    /**
     * Check if AI processing is currently running
     */
    fun isProcessing(): Boolean {
        return aiScope.isActive
    }
    
    /**
     * Cancel all AI operations
     */
    fun cancelAll() {
        aiScope.coroutineContext.cancelChildren()
    }
    
    /**
     * Clean up resources when service is no longer needed
     */
    fun cleanup() {
        aiScope.cancel()
        (aiDispatcher as? ExecutorCoroutineDispatcher)?.close()
    }
}

/**
 * AI operation states for UI feedback
 */
sealed class AIOperationState {
    object Idle : AIOperationState()
    data class Processing(val progress: Int) : AIOperationState()
    data class Success<T>(val result: T) : AIOperationState()
    data class Error(val exception: Exception) : AIOperationState()
}