package com.heartlessveteran.myriad.core.domain.ai

import com.heartlessveteran.myriad.core.domain.model.Result
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Test for AIProvider interface functionality
 */
class AIProviderTest {

    private val testAIProvider = object : AIProvider {
        override val name = "Test Provider"
        override val isAvailable = true

        override suspend fun generateResponse(prompt: String): Result<String> {
            return Result.Success("Test response to: $prompt")
        }

        override suspend fun generateRecommendations(
            userPreferences: Map<String, Any>,
            readingHistory: List<String>
        ): Result<List<String>> {
            return Result.Success(listOf("Test Recommendation 1", "Test Recommendation 2"))
        }

        override suspend fun analyzeArtStyle(imageData: ByteArray): Result<List<String>> {
            return Result.Success(listOf("Test Style 1", "Test Style 2"))
        }

        override suspend fun translateText(imageData: ByteArray): Result<String> {
            return Result.Success("Test translation")
        }
    }

    @Test
    fun `test provider properties`() {
        assertEquals("Test Provider", testAIProvider.name)
        assertTrue(testAIProvider.isAvailable)
    }

    @Test
    fun `test generateResponse returns success`() = runTest {
        val result = testAIProvider.generateResponse("test prompt")
        
        assertTrue(result.isSuccess())
        assertEquals("Test response to: test prompt", result.getOrNull())
    }

    @Test
    fun `test generateRecommendations returns success`() = runTest {
        val result = testAIProvider.generateRecommendations(mapOf("genre" to "action"))
        
        assertTrue(result.isSuccess())
        val recommendations = result.getOrNull()
        assertNotNull(recommendations)
        assertEquals(2, recommendations?.size)
        assertEquals("Test Recommendation 1", recommendations?.get(0))
    }

    @Test
    fun `test analyzeArtStyle returns success`() = runTest {
        val result = testAIProvider.analyzeArtStyle(byteArrayOf())
        
        assertTrue(result.isSuccess())
        val styles = result.getOrNull()
        assertNotNull(styles)
        assertEquals(2, styles?.size)
        assertEquals("Test Style 1", styles?.get(0))
    }

    @Test
    fun `test translateText returns success`() = runTest {
        val result = testAIProvider.translateText(byteArrayOf())
        
        assertTrue(result.isSuccess())
        assertEquals("Test translation", result.getOrNull())
    }
}