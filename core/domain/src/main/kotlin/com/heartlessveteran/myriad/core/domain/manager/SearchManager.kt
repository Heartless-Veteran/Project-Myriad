package com.heartlessveteran.myriad.core.domain.manager

import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Search result containing manga with source information
 */
data class SearchResult(
    val manga: Manga,
    val sourceId: String,
    val sourceName: String
)

/**
 * Grouped search results by source
 */
data class GroupedSearchResults(
    val results: Map<String, List<SearchResult>>,
    val totalCount: Int,
    val hasMore: Boolean
)

/**
 * Search filters for manga
 */
data class SearchFilters(
    val query: String,
    val genres: List<String> = emptyList(),
    val excludedGenres: List<String> = emptyList(),
    val status: String? = null,
    val language: String? = null,
    val sourceIds: List<String> = emptyList(), // Empty means all enabled sources
    val sortBy: SearchSortBy = SearchSortBy.RELEVANCE
)

/**
 * Search sorting options
 */
enum class SearchSortBy {
    RELEVANCE,
    TITLE,
    LATEST_UPDATED,
    RATING,
    POPULARITY
}

/**
 * Manager interface for global search across all enabled sources
 */
interface SearchManager {
    /**
     * Searches manga across all enabled sources with filters
     * @param filters Search filters and query
     * @param page Page number for pagination
     * @return Flow of grouped search results
     */
    fun searchManga(filters: SearchFilters, page: Int = 1): Flow<Result<GroupedSearchResults>>

    /**
     * Searches manga in a specific source
     * @param sourceId Source identifier
     * @param query Search query
     * @param page Page number
     * @return Result containing list of search results
     */
    suspend fun searchInSource(sourceId: String, query: String, page: Int = 1): Result<List<SearchResult>>

    /**
     * Gets latest manga from all enabled sources
     * @param page Page number
     * @return Flow of grouped results
     */
    fun getLatestManga(page: Int = 1): Flow<Result<GroupedSearchResults>>

    /**
     * Gets popular manga from all enabled sources
     * @param page Page number
     * @return Flow of grouped results
     */
    fun getPopularManga(page: Int = 1): Flow<Result<GroupedSearchResults>>

    /**
     * Cancels ongoing search operations
     */
    suspend fun cancelSearch()
}