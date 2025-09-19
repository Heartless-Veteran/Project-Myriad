package com.heartlessveteran.myriad.network

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Gemini API data classes to ensure proper serialization/deserialization.
 */
class GeminiServiceDataClassTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        }

    @Test
    fun `GeminiChatRequest serializes correctly`() {
        val request =
            GeminiChatRequest(
                contents =
                    listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart("Hello, Gemini!")),
                            role = "user",
                        ),
                    ),
                generationConfig =
                    GeminiGenerationConfig(
                        temperature = 0.7f,
                        maxOutputTokens = 100,
                    ),
            )

        val jsonString = json.encodeToString(request)

        assertTrue("JSON should contain 'Hello, Gemini!'", jsonString.contains("Hello, Gemini!"))
        assertTrue("JSON should contain temperature", jsonString.contains("temperature"))
        assertTrue("JSON should contain maxOutputTokens", jsonString.contains("maxOutputTokens"))
    }

    @Test
    fun `GeminiChatResponse deserializes correctly`() {
        val responseJson =
            """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "Hello! How can I help you today?"
                      }
                    ],
                    "role": "model"
                  },
                  "finishReason": "STOP",
                  "index": 0
                }
              ]
            }
            """.trimIndent()

        val response = json.decodeFromString<GeminiChatResponse>(responseJson)

        assertEquals("Should have one candidate", 1, response.candidates.size)

        val candidate = response.candidates.first()
        assertEquals("Should have correct finish reason", "STOP", candidate.finishReason)
        assertEquals("Should have correct index", 0, candidate.index)
        assertEquals("Should have correct role", "model", candidate.content.role)
        assertEquals("Should have one text part", 1, candidate.content.parts.size)
        assertEquals(
            "Should have correct text",
            "Hello! How can I help you today?",
            candidate.content.parts
                .first()
                .text,
        )
    }

    @Test
    fun `GeminiGenerationConfig uses correct defaults`() {
        val config = GeminiGenerationConfig()

        assertEquals("Default temperature should be 0.7f", 0.7f, config.temperature!!, 0.001f)
        assertEquals("Default topK should be 40", 40, config.topK)
        assertEquals("Default topP should be 0.95f", 0.95f, config.topP!!, 0.001f)
        assertEquals("Default maxOutputTokens should be 1024", 1024, config.maxOutputTokens)
    }
}
