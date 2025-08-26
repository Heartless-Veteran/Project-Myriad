package com.heartlessveteran.myriad.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Constants for Gemini API configuration
 */
private const val GEMINI_GENERATE_CONTENT_ENDPOINT = "v1/models/gemini-pro:generateContent"
private const val DEFAULT_TEMPERATURE = 0.7f
private const val DEFAULT_TOP_K = 40
private const val DEFAULT_TOP_P = 0.9f
private const val DEFAULT_MAX_OUTPUT_TOKENS = 1000

/**
 * Retrofit service interface for Google Gemini API chat endpoints.
 */
interface GeminiService {
    
    /**
     * Send a chat request to the Gemini API and receive a response.
     * 
     * @param request The chat request containing the message and configuration
     * @return The chat response from Gemini API
     */
    @POST(GEMINI_GENERATE_CONTENT_ENDPOINT)
    suspend fun generateContent(@Body request: GeminiChatRequest): GeminiChatResponse
}

/**
 * Request data class for Gemini API chat requests.
 * 
 * @property contents List of content parts to send to Gemini
 * @property generationConfig Configuration for content generation
 */
@Serializable
data class GeminiChatRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

/**
 * Content part of a Gemini API request.
 * 
 * @property parts List of text parts in the content
 * @property role The role of the content sender (e.g., "user")
 */
@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

/**
 * Text part within content.
 * 
 * @property text The text content to send
 */
@Serializable
data class GeminiPart(
    val text: String
)

/**
 * Configuration for Gemini content generation.
 * 
 * @property temperature Controls randomness in responses (0.0-1.0)
 * @property topK Number of highest probability vocabulary tokens to keep for top-k filtering
 * @property topP Nucleus sampling parameter (0.0-1.0)
 * @property maxOutputTokens Maximum number of tokens to generate
 */
@Serializable
data class GeminiGenerationConfig(
    val temperature: Float? = DEFAULT_TEMPERATURE,
    val topK: Int? = DEFAULT_TOP_K,
    val topP: Float? = DEFAULT_TOP_P,
    val maxOutputTokens: Int? = DEFAULT_MAX_OUTPUT_TOKENS
)

/**
 * Response data class for Gemini API chat responses.
 * 
 * @property candidates List of generated response candidates
 */
@Serializable
data class GeminiChatResponse(
    val candidates: List<GeminiCandidate>
)

/**
 * Candidate response from Gemini API.
 * 
 * @property content The generated content
 * @property finishReason Reason why generation finished
 * @property index Index of the candidate
 */
@Serializable
data class GeminiCandidate(
    val content: GeminiContent,
    val finishReason: String? = null,
    val index: Int? = null
)