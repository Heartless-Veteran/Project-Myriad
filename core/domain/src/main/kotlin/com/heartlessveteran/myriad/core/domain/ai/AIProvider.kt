package com.heartlessveteran.myriad.core.domain.ai

import com.heartlessveteran.myriad.core.domain.model.Result

/**
 * Represents an AI provider that can generate responses for manga-related queries.
 * This interface allows for flexibility in supporting multiple AI models like Gemini, OpenAI, etc.
 */
interface AIProvider {
    val name: String
    val isAvailable: Boolean

    /**
     * Generate a text response based on the provided prompt
     */
    suspend fun generateResponse(prompt: String): Result<String>

    /**
     * Generate recommendations based on user preferences and reading history
     */
    suspend fun generateRecommendations(
        userPreferences: Map<String, Any>,
        readingHistory: List<String> = emptyList()
    ): Result<List<String>>

    /**
     * Analyze art style from image data
     */
    suspend fun analyzeArtStyle(imageData: ByteArray): Result<List<String>>

    /**
     * Perform OCR translation on image data
     */
    suspend fun translateText(imageData: ByteArray): Result<String>
}