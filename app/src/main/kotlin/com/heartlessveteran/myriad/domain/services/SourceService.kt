package com.heartlessveteran.myriad.domain.services

import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.models.Result
import kotlinx.coroutines.flow.Flow

/**
 * Source management service for handling online content sources.
 * 
 * This service provides functionality for:
 * - Managing multiple content sources (MangaDx, Komikku, etc.)
 * - Plugin/extension system for custom sources
 * - Source configuration and preferences
 * - Unified search across sources
 */
interface SourceService {
    
    /**
     * Get all available content sources.
     * 
     * @return List of available sources
     */
    fun getAvailableSources(): List<ContentSource>
    
    /**
     * Get enabled content sources only.
     * 
     * @return List of enabled sources
     */
    fun getEnabledSources(): List<ContentSource>
    
    /**
     * Enable or disable a content source.
     * 
     * @param sourceId The source identifier
     * @param enabled Whether to enable or disable the source
     * @return Result indicating success or failure
     */
    suspend fun setSourceEnabled(sourceId: String, enabled: Boolean): Result<Unit>
    
    /**
     * Search for manga across all enabled sources.
     * 
     * @param query Search query
     * @param sources Specific sources to search, or null for all enabled
     * @return Flow emitting search results from each source
     */
    fun searchMangaAcrossSources(
        query: String,
        sources: List<String>? = null
    ): Flow<SourceSearchResult>
    
    /**
     * Get latest manga from a specific source.
     * 
     * @param sourceId The source identifier
     * @param page Page number for pagination
     * @return Result containing manga list or error
     */
    suspend fun getLatestManga(sourceId: String, page: Int = 1): Result<List<Manga>>
    
    /**
     * Get popular manga from a specific source.
     * 
     * @param sourceId The source identifier
     * @param page Page number for pagination
     * @return Result containing manga list or error
     */
    suspend fun getPopularManga(sourceId: String, page: Int = 1): Result<List<Manga>>
    
    /**
     * Get manga details from a source.
     * 
     * @param sourceId The source identifier
     * @param mangaId The manga ID on the source
     * @return Result containing manga details or error
     */
    suspend fun getMangaDetails(sourceId: String, mangaId: String): Result<Manga>
    
    /**
     * Get manga chapters from a source.
     * 
     * @param sourceId The source identifier
     * @param mangaId The manga ID on the source
     * @return Result containing chapter list or error
     */
    suspend fun getMangaChapters(
        sourceId: String, 
        mangaId: String
    ): Result<List<com.heartlessveteran.myriad.domain.entities.MangaChapter>>
    
    /**
     * Configure source-specific settings.
     * 
     * @param sourceId The source identifier
     * @param settings Map of setting key-value pairs
     * @return Result indicating success or failure
     */
    suspend fun configureSource(
        sourceId: String, 
        settings: Map<String, Any>
    ): Result<Unit>
    
    /**
     * Install a new source plugin/extension.
     * 
     * @param extensionPath Path to the extension file
     * @return Result containing installed source info or error
     */
    suspend fun installSourceExtension(extensionPath: String): Result<ContentSource>
    
    /**
     * Uninstall a source extension.
     * 
     * @param sourceId The source identifier to uninstall
     * @return Result indicating success or failure
     */
    suspend fun uninstallSourceExtension(sourceId: String): Result<Unit>
    
    /**
     * Check for source updates.
     * 
     * @return Flow emitting update information for sources
     */
    fun checkForSourceUpdates(): Flow<SourceUpdateInfo>
}

/**
 * Represents a content source (MangaDx, Komikku, etc.).
 */
data class ContentSource(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val isEnabled: Boolean = true,
    val isOfficial: Boolean = true,
    val hasSettings: Boolean = false,
    val supportedFeatures: Set<SourceFeature> = emptySet(),
    val iconUrl: String? = null
)

/**
 * Features that a content source can support.
 */
enum class SourceFeature {
    SEARCH,
    LATEST,
    POPULAR, 
    DETAILS,
    CHAPTERS,
    FILTERING,
    LOGIN_REQUIRED,
    RATE_LIMITED,
    NSFW_CONTENT
}

/**
 * Search result from a specific source.
 */
data class SourceSearchResult(
    val sourceId: String,
    val sourceName: String,
    val results: List<Manga>,
    val hasMore: Boolean = false,
    val error: String? = null
)

/**
 * Update information for a source.
 */
data class SourceUpdateInfo(
    val sourceId: String,
    val currentVersion: String,
    val latestVersion: String,
    val updateAvailable: Boolean,
    val updateDescription: String? = null
)