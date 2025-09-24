package com.heartlessveteran.myriad.ui.viewmodel

import com.heartlessveteran.myriad.core.domain.manager.SearchManager
import com.heartlessveteran.myriad.core.domain.manager.SearchFilters
import com.heartlessveteran.myriad.core.domain.manager.GroupedSearchResults
import com.heartlessveteran.myriad.core.domain.manager.SearchResult
import com.heartlessveteran.myriad.core.domain.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Temporary mock SearchManager for build compatibility
 */
class MockSearchManager : SearchManager {
    override fun searchManga(filters: SearchFilters, page: Int): Flow<Result<GroupedSearchResults>> {
        return flowOf(Result.Success(GroupedSearchResults(emptyMap(), 0, false)))
    }

    override suspend fun searchInSource(sourceId: String, query: String, page: Int): Result<List<SearchResult>> {
        return Result.Success(emptyList())
    }

    override fun getPopularManga(page: Int): Flow<Result<GroupedSearchResults>> {
        return flowOf(Result.Success(GroupedSearchResults(emptyMap(), 0, false)))
    }

    override fun getLatestManga(page: Int): Flow<Result<GroupedSearchResults>> {
        return flowOf(Result.Success(GroupedSearchResults(emptyMap(), 0, false)))
    }

    override suspend fun cancelSearch() {
        // No-op for mock implementation
    }
}