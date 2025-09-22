package com.heartlessveteran.myriad.core.data.manager

import com.heartlessveteran.myriad.core.domain.manager.GroupedSearchResults
import com.heartlessveteran.myriad.core.domain.manager.PluginManager
import com.heartlessveteran.myriad.core.domain.manager.SearchFilters
import com.heartlessveteran.myriad.core.domain.manager.SearchManager
import com.heartlessveteran.myriad.core.domain.manager.SearchResult
import com.heartlessveteran.myriad.core.domain.model.Result
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first

/**
 * Implementation of SearchManager for global search across all enabled sources
 */
class SearchManagerImpl(
    private val pluginManager: PluginManager
) : SearchManager {

    private var searchJob: Job? = null

    override fun searchManga(filters: SearchFilters, page: Int): Flow<Result<GroupedSearchResults>> = flow {
        emit(Result.Loading)
        
        try {
            val sources = pluginManager.getEnabledSources().first()
            val filteredSources = if (filters.sourceIds.isNotEmpty()) {
                sources.filter { source -> source.id in filters.sourceIds }
            } else {
                sources
            }

            if (filteredSources.isEmpty()) {
                emit(Result.Success(GroupedSearchResults(emptyMap(), 0, false)))
                return@flow
            }

            // Execute searches in parallel across all enabled sources
            val searchResults = coroutineScope {
                filteredSources.map { source ->
                    async {
                    try {
                        val result = source.searchManga(filters.query, page)
                        when (result) {
                            is Result.Success -> {
                                val searchResults = result.data.map { manga ->
                                    SearchResult(
                                        manga = manga.copy(source = source.id),
                                        sourceId = source.id,
                                        sourceName = source.name
                                    )
                                }
                                source.id to searchResults
                            }
                            is Result.Error -> {
                                // Log error but don't fail the entire search
                                source.id to emptyList<SearchResult>()
                            }
                            is Result.Loading -> source.id to emptyList<SearchResult>()
                        }
                    } catch (e: Exception) {
                        // Handle individual source failures gracefully
                        source.id to emptyList<SearchResult>()
                    }
                }
                }.awaitAll().toMap()
            }

            // Apply additional filters
            val filteredResults = searchResults.mapValues { (_, results) ->
                applyFilters(results, filters)
            }.filterValues { it.isNotEmpty() }

            val totalCount = filteredResults.values.sumOf { it.size }
            val groupedResults = GroupedSearchResults(
                results = filteredResults,
                totalCount = totalCount,
                hasMore = totalCount >= 20 // Simple pagination check
            )

            emit(Result.Success(groupedResults))
        } catch (e: Exception) {
            emit(Result.Error(e, "Search failed: ${e.message}"))
        }
    }

    override suspend fun searchInSource(sourceId: String, query: String, page: Int): Result<List<SearchResult>> {
        return try {
            val sourceResult = pluginManager.getSourceByPluginId(sourceId)
            when (sourceResult) {
                is Result.Success -> {
                    val source = sourceResult.data
                    val result = source.searchManga(query, page)
                    when (result) {
                        is Result.Success -> {
                            val searchResults = result.data.map { manga ->
                                SearchResult(
                                    manga = manga.copy(source = source.id),
                                    sourceId = source.id,
                                    sourceName = source.name
                                )
                            }
                            Result.Success(searchResults)
                        }
                        is Result.Error -> result
                        is Result.Loading -> Result.Error(IllegalStateException("Unexpected loading state"), "Search is still in progress")
                    }
                }
                is Result.Error -> sourceResult
                is Result.Loading -> Result.Error(IllegalStateException("Unexpected loading state"), "Plugin manager is still loading")
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to search in source $sourceId: ${e.message}")
        }
    }

    override fun getLatestManga(page: Int): Flow<Result<GroupedSearchResults>> = flow {
        emit(Result.Loading)
        
        try {
            val sources = pluginManager.getEnabledSources().first()

            if (sources.isEmpty()) {
                emit(Result.Success(GroupedSearchResults(emptyMap(), 0, false)))
                return@flow
            }

            // Get latest manga from all sources in parallel
            val latestResults = coroutineScope {
                sources.map { source ->
                    async {
                    try {
                        val result = source.getLatestManga(page)
                        when (result) {
                            is Result.Success -> {
                                val searchResults = result.data.map { manga ->
                                    SearchResult(
                                        manga = manga.copy(source = source.id),
                                        sourceId = source.id,
                                        sourceName = source.name
                                    )
                                }
                                source.id to searchResults
                            }
                            is Result.Error -> source.id to emptyList<SearchResult>()
                            is Result.Loading -> source.id to emptyList<SearchResult>()
                        }
                    } catch (e: Exception) {
                        source.id to emptyList<SearchResult>()
                    }
                }
                }.awaitAll().toMap().filterValues { it.isNotEmpty() }
            }

            val totalCount = latestResults.values.sumOf { it.size }
            val groupedResults = GroupedSearchResults(
                results = latestResults,
                totalCount = totalCount,
                hasMore = totalCount >= 20
            )

            emit(Result.Success(groupedResults))
        } catch (e: Exception) {
            emit(Result.Error(e, "Failed to get latest manga: ${e.message}"))
        }
    }

    override fun getPopularManga(page: Int): Flow<Result<GroupedSearchResults>> = flow {
        emit(Result.Loading)
        
        try {
            val sources = pluginManager.getEnabledSources().first()

            if (sources.isEmpty()) {
                emit(Result.Success(GroupedSearchResults(emptyMap(), 0, false)))
                return@flow
            }

            // Get popular manga from all sources in parallel
            val popularResults = coroutineScope {
                sources.map { source ->
                    async {
                    try {
                        val result = source.getPopularManga(page)
                        when (result) {
                            is Result.Success -> {
                                val searchResults = result.data.map { manga ->
                                    SearchResult(
                                        manga = manga.copy(source = source.id),
                                        sourceId = source.id,
                                        sourceName = source.name
                                    )
                                }
                                source.id to searchResults
                            }
                            is Result.Error -> source.id to emptyList<SearchResult>()
                            is Result.Loading -> source.id to emptyList<SearchResult>()
                        }
                    } catch (e: Exception) {
                        source.id to emptyList<SearchResult>()
                    }
                }
                }.awaitAll().toMap().filterValues { it.isNotEmpty() }
            }

            val totalCount = popularResults.values.sumOf { it.size }
            val groupedResults = GroupedSearchResults(
                results = popularResults,
                totalCount = totalCount,
                hasMore = totalCount >= 20
            )

            emit(Result.Success(groupedResults))
        } catch (e: Exception) {
            emit(Result.Error(e, "Failed to get popular manga: ${e.message}"))
        }
    }

    override suspend fun cancelSearch() {
        searchJob?.cancel()
        searchJob = null
    }

    /**
     * Applies additional filters to search results
     */
    private fun applyFilters(results: List<SearchResult>, filters: SearchFilters): List<SearchResult> {
        return results.filter { result ->
            val manga = result.manga
            
            // Apply genre filters
            val genreMatch = if (filters.genres.isNotEmpty()) {
                filters.genres.any { genre -> manga.genres.contains(genre) }
            } else true
            
            // Apply excluded genre filters
            val excludedGenreMatch = if (filters.excludedGenres.isNotEmpty()) {
                !filters.excludedGenres.any { genre -> manga.genres.contains(genre) }
            } else true
            
            // Apply status filter
            val statusMatch = if (filters.status != null) {
                manga.status.name.equals(filters.status, ignoreCase = true)
            } else true
            
            genreMatch && excludedGenreMatch && statusMatch
        }
    }
}