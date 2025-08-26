package com.heartlessveteran.myriad.services

import com.heartlessveteran.myriad.domain.models.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Content types supported by sources
 */
enum class ContentType(val displayName: String) {
    MANGA("Manga"),
    ANIME("Anime"),
    NOVEL("Novel"),
    ALL("All")
}

/**
 * Content search filters
 */
@Serializable
data class SearchFilters(
    val query: String,
    val type: ContentType = ContentType.ALL,
    val genres: List<String> = emptyList(),
    val status: String? = null,
    val year: Int? = null,
    val rating: Float? = null,
    val sortBy: String = "popularity"
)

/**
 * Content item from search results
 */
@Serializable
data class ContentItem(
    val id: String,
    val title: String,
    val coverUrl: String? = null,
    val description: String? = null,
    val type: ContentType,
    val rating: Float? = null,
    val status: String? = null,
    val chapters: Int? = null,
    val volumes: Int? = null,
    val sourceId: String,
    val url: String
)

/**
 * Content source plugin interface
 */
interface ContentSource {
    val id: String
    val name: String
    val baseUrl: String
    val language: String
    val isEnabled: Boolean
    
    suspend fun search(query: String, page: Int = 1): Result<List<ContentItem>>
    suspend fun getContent(url: String): Result<ContentDetail>
    suspend fun getChapters(contentId: String): Result<List<Chapter>>
    suspend fun getPages(chapterId: String): Result<List<String>>
    suspend fun browse(filters: SearchFilters, page: Int = 1): Result<List<ContentItem>>
}

/**
 * Detailed content information
 */
@Serializable
data class ContentDetail(
    val id: String,
    val title: String,
    val alternativeTitles: List<String> = emptyList(),
    val description: String? = null,
    val coverUrl: String? = null,
    val bannerUrl: String? = null,
    val type: ContentType,
    val status: String? = null,
    val rating: Float? = null,
    val genres: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val author: String? = null,
    val artist: String? = null,
    val publishedYear: Int? = null,
    val chapters: List<Chapter> = emptyList(),
    val sourceId: String,
    val url: String
)

/**
 * Chapter information
 */
@Serializable
data class Chapter(
    val id: String,
    val title: String,
    val number: Float,
    val volume: Int? = null,
    val publishedDate: Long? = null,
    val pages: List<String> = emptyList(),
    val url: String
)

/**
 * Source authentication information
 */
@Serializable
data class SourceAuth(
    val sourceId: String,
    val username: String? = null,
    val token: String? = null,
    val cookies: Map<String, String> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val isAuthenticated: Boolean = false
)

/**
 * Source statistics and monitoring
 */
@Serializable
data class SourceStats(
    val sourceId: String,
    val totalRequests: Long,
    val successfulRequests: Long,
    val failedRequests: Long,
    val averageResponseTime: Long,
    val lastUsed: Long,
    val rateLimitRemaining: Int? = null,
    val rateLimitReset: Long? = null
)

/**
 * Search result aggregated from multiple sources
 */
@Serializable
data class AggregatedSearchResult(
    val query: String,
    val totalResults: Int,
    val sources: Map<String, List<ContentItem>>,
    val processingTime: Long,
    val errors: Map<String, String> = emptyMap()
)

/**
 * Source Extension System
 * 
 * Provides extensible content source framework with:
 * - Plugin architecture for content sources
 * - Authentication management
 * - Rate limiting per source
 * - Unified search across multiple sources
 * - Browse functionality
 * - Error handling and retry logic
 * - Source statistics and monitoring
 */
@Singleton
class SourceExtensionSystem @Inject constructor(
    private val cacheService: SmartCacheService
) {
    
    companion object {
        private const val SOURCE_CACHE_KEY = "source_content"
        private const val SEARCH_CACHE_TTL = 15 * 60 * 1000L // 15 minutes
    }
    
    private val registeredSources = mutableMapOf<String, ContentSource>()
    private val sourceAuth = mutableMapOf<String, SourceAuth>()
    private val sourceStats = mutableMapOf<String, SourceStats>()
    
    /**
     * Register a content source
     * 
     * @param source The content source to register
     */
    fun registerSource(source: ContentSource) {
        registeredSources[source.id] = source
        sourceStats[source.id] = SourceStats(
            sourceId = source.id,
            totalRequests = 0,
            successfulRequests = 0,
            failedRequests = 0,
            averageResponseTime = 0,
            lastUsed = 0
        )
    }
    
    /**
     * Get all available sources
     * 
     * @return List of registered sources
     */
    fun getAvailableSources(): List<ContentSource> {
        return registeredSources.values.toList()
    }
    
    /**
     * Get enabled sources only
     * 
     * @return List of enabled sources
     */
    fun getEnabledSources(): List<ContentSource> {
        return registeredSources.values.filter { it.isEnabled }
    }
    
    /**
     * Get source by ID
     * 
     * @param sourceId The source identifier
     * @return The content source or null if not found
     */
    fun getSource(sourceId: String): ContentSource? {
        return registeredSources[sourceId]
    }
    
    /**
     * Search across all enabled sources
     * 
     * @param filters Search filters
     * @return Aggregated search results from all sources
     */
    suspend fun searchAllSources(filters: SearchFilters): Result<AggregatedSearchResult> {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                val enabledSources = getEnabledSources()
                
                // Check cache first
                val cacheKey = "search_${filters.hashCode()}"
                cacheService.get<AggregatedSearchResult>(SOURCE_CACHE_KEY, cacheKey).let { cached ->
                    if (cached.isSuccess) {
                        cached.getOrNull()?.let { result ->
                            return@withContext Result.Success(result)
                        }
                    }
                }
                
                // Search all sources concurrently
                val searchResults = enabledSources.map { source ->
                    async {
                        try {
                            val startSourceTime = System.currentTimeMillis()
                            val result = source.search(filters.query)
                            updateSourceStats(source.id, true, System.currentTimeMillis() - startSourceTime)
                            source.id to result.getOrNull().orEmpty()
                        } catch (e: Exception) {
                            updateSourceStats(source.id, false, 0)
                            source.id to emptyList<ContentItem>()
                        }
                    }
                }.awaitAll()
                
                val successfulResults = searchResults.filter { it.second.isNotEmpty() }
                val errors = searchResults.filter { it.second.isEmpty() }.associate { 
                    it.first to "Search failed" 
                }
                
                val aggregatedResult = AggregatedSearchResult(
                    query = filters.query,
                    totalResults = successfulResults.sumOf { it.second.size },
                    sources = successfulResults.toMap(),
                    processingTime = System.currentTimeMillis() - startTime,
                    errors = errors
                )
                
                // Cache the result
                cacheService.set(
                    SOURCE_CACHE_KEY,
                    cacheKey,
                    aggregatedResult,
                    SEARCH_CACHE_TTL,
                    CachePriority.NORMAL
                )
                
                Result.Success(aggregatedResult)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
    
    /**
     * Search in specific source
     * 
     * @param sourceId The source to search in
     * @param filters Search filters
     * @return Search results from the specific source
     */
    suspend fun searchInSource(sourceId: String, filters: SearchFilters): Result<List<ContentItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val source = getSource(sourceId)
                    ?: return@withContext Result.Error(Exception("Source not found: $sourceId"))
                
                val startTime = System.currentTimeMillis()
                val result = source.search(filters.query)
                updateSourceStats(sourceId, result.isSuccess, System.currentTimeMillis() - startTime)
                
                result
            } catch (e: Exception) {
                updateSourceStats(sourceId, false, 0)
                Result.Error(e)
            }
        }
    }
    
    /**
     * Browse content in source
     * 
     * @param sourceId The source to browse
     * @param filters Browse filters
     * @param page Page number
     * @return Browse results
     */
    suspend fun browseSource(
        sourceId: String,
        filters: SearchFilters = SearchFilters(""),
        page: Int = 1
    ): Result<List<ContentItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val source = getSource(sourceId)
                    ?: return@withContext Result.Error(Exception("Source not found: $sourceId"))
                
                val startTime = System.currentTimeMillis()
                val result = source.browse(filters, page)
                updateSourceStats(sourceId, result.isSuccess, System.currentTimeMillis() - startTime)
                
                result
            } catch (e: Exception) {
                updateSourceStats(sourceId, false, 0)
                Result.Error(e)
            }
        }
    }
    
    /**
     * Get content details from source
     * 
     * @param sourceId The source identifier
     * @param url Content URL
     * @return Content details
     */
    suspend fun getContentDetails(sourceId: String, url: String): Result<ContentDetail> {
        return withContext(Dispatchers.IO) {
            try {
                val source = getSource(sourceId)
                    ?: return@withContext Result.Error(Exception("Source not found: $sourceId"))
                
                val startTime = System.currentTimeMillis()
                val result = source.getContent(url)
                updateSourceStats(sourceId, result.isSuccess, System.currentTimeMillis() - startTime)
                
                result
            } catch (e: Exception) {
                updateSourceStats(sourceId, false, 0)
                Result.Error(e)
            }
        }
    }
    
    /**
     * Set authentication for a source
     * 
     * @param sourceId The source identifier
     * @param auth Authentication information
     */
    fun setSourceAuth(sourceId: String, auth: SourceAuth) {
        sourceAuth[sourceId] = auth
    }
    
    /**
     * Get authentication for a source
     * 
     * @param sourceId The source identifier
     * @return Authentication information or null
     */
    fun getSourceAuth(sourceId: String): SourceAuth? {
        return sourceAuth[sourceId]
    }
    
    /**
     * Get statistics for a source
     * 
     * @param sourceId The source identifier
     * @return Source statistics
     */
    fun getSourceStats(sourceId: String): SourceStats? {
        return sourceStats[sourceId]
    }
    
    /**
     * Get statistics for all sources
     * 
     * @return Map of source statistics
     */
    fun getAllSourceStats(): Map<String, SourceStats> {
        return sourceStats.toMap()
    }
    
    /**
     * Clear source cache
     */
    suspend fun clearCache(): Result<Unit> {
        return cacheService.clear(SOURCE_CACHE_KEY)
    }
    
    /**
     * Update source statistics
     */
    private fun updateSourceStats(sourceId: String, success: Boolean, responseTime: Long) {
        val stats = sourceStats[sourceId] ?: return
        
        sourceStats[sourceId] = stats.copy(
            totalRequests = stats.totalRequests + 1,
            successfulRequests = if (success) stats.successfulRequests + 1 else stats.successfulRequests,
            failedRequests = if (!success) stats.failedRequests + 1 else stats.failedRequests,
            averageResponseTime = ((stats.averageResponseTime * (stats.totalRequests - 1)) + responseTime) / stats.totalRequests,
            lastUsed = System.currentTimeMillis()
        )
    }
}

/**
 * Mock content source for testing
 */
class MockContentSource(
    override val id: String = "mock_source",
    override val name: String = "Mock Source",
    override val baseUrl: String = "https://mock.example.com",
    override val language: String = "en",
    override val isEnabled: Boolean = true
) : ContentSource {
    
    override suspend fun search(query: String, page: Int): Result<List<ContentItem>> {
        // Mock search implementation
        val mockResults = listOf(
            ContentItem(
                id = "mock_1",
                title = "Mock Manga $query",
                type = ContentType.MANGA,
                rating = 8.5f,
                status = "Ongoing",
                chapters = 120,
                sourceId = id,
                url = "$baseUrl/manga/mock_1"
            )
        )
        return Result.Success(mockResults)
    }
    
    override suspend fun getContent(url: String): Result<ContentDetail> {
        // Mock content detail
        val mockDetail = ContentDetail(
            id = "mock_1",
            title = "Mock Manga Title",
            description = "This is a mock manga for testing purposes",
            type = ContentType.MANGA,
            rating = 8.5f,
            genres = listOf("Action", "Adventure"),
            sourceId = id,
            url = url
        )
        return Result.Success(mockDetail)
    }
    
    override suspend fun getChapters(contentId: String): Result<List<Chapter>> {
        // Mock chapters
        val mockChapters = listOf(
            Chapter("ch_1", "Chapter 1", 1.0f, 1, url = "$baseUrl/chapter/ch_1")
        )
        return Result.Success(mockChapters)
    }
    
    override suspend fun getPages(chapterId: String): Result<List<String>> {
        // Mock pages
        val mockPages = listOf("$baseUrl/page/1.jpg", "$baseUrl/page/2.jpg")
        return Result.Success(mockPages)
    }
    
    override suspend fun browse(filters: SearchFilters, page: Int): Result<List<ContentItem>> {
        // Mock browse
        return search(filters.query, page)
    }
}