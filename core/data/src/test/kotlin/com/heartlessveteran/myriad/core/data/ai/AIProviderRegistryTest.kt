package com.heartlessveteran.myriad.core.data.ai

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Test for AIProviderRegistry functionality
 */
class AIProviderRegistryTest {

    private val geminiProvider = GeminiProvider()
    private val openAIProvider = OpenAIProvider()
    private val registry = AIProviderRegistry(geminiProvider, openAIProvider)

    @Test
    fun `test getProviders returns available providers`() {
        val providers = registry.getProviders()
        
        assertEquals(2, providers.size)
        assertTrue(providers.any { it.name == "Gemini" })
        assertTrue(providers.any { it.name == "OpenAI" })
    }

    @Test
    fun `test getProviderByName returns correct provider`() {
        val gemini = registry.getProviderByName("Gemini")
        val openAI = registry.getProviderByName("OpenAI")
        val nonExistent = registry.getProviderByName("NonExistent")
        
        assertNotNull(gemini)
        assertEquals("Gemini", gemini?.name)
        
        assertNotNull(openAI)
        assertEquals("OpenAI", openAI?.name)
        
        assertNull(nonExistent)
    }

    @Test
    fun `test getDefaultProvider returns first available provider`() {
        val defaultProvider = registry.getDefaultProvider()
        
        assertNotNull(defaultProvider)
        assertEquals("Gemini", defaultProvider.name) // Should be first in the list
    }

    @Test
    fun `test hasAvailableProviders returns true when providers exist`() {
        assertTrue(registry.hasAvailableProviders())
    }

    @Test
    fun `test gemini provider basic functionality`() = runTest {
        val result = geminiProvider.generateResponse("test prompt")
        
        assertTrue(result.isSuccess())
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response!!.contains("Gemini response"))
    }

    @Test
    fun `test openAI provider basic functionality`() = runTest {
        val result = openAIProvider.generateResponse("test prompt")
        
        assertTrue(result.isSuccess())
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response!!.contains("OpenAI response"))
    }
}