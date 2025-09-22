package com.heartlessveteran.myriad.feature.browser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.core.domain.manager.*
import com.heartlessveteran.myriad.core.domain.model.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI state for global search screen
 */
data class GlobalSearchUiState(
    val query: String = "",
    val searchResults: GroupedSearchResults = GroupedSearchResults(emptyMap(), 0, false),
    val isSearching: Boolean = false,
    val error: String? = null,
    val searchFilters: SearchFilters = SearchFilters(""),
    val showFilters: Boolean = false,
    val selectedTab: SearchTab = SearchTab.SEARCH
)

/**
 * Search tab types
 */
enum class SearchTab {
    SEARCH,
    LATEST,
    POPULAR
}

/**
 * ViewModel for global search functionality
 */
class GlobalSearchViewModel(
    private val searchManager: SearchManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GlobalSearchUiState())
    val uiState: StateFlow<GlobalSearchUiState> = _uiState.asStateFlow()

    /**
     * Updates the search query
     */
    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            query = query,
            searchFilters = _uiState.value.searchFilters.copy(query = query)
        )
    }

    /**
     * Performs search with current filters
     */
    fun search() {
        val currentState = _uiState.value
        if (currentState.query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)
            
            searchManager.searchManga(currentState.searchFilters)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        error = "Search failed: ${e.message}"
                    )
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.value = _uiState.value.copy(
                                searchResults = result.data,
                                isSearching = false,
                                error = null,
                                selectedTab = SearchTab.SEARCH
                            )
                        }
                        is Result.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isSearching = false,
                                error = result.message ?: "Search failed"
                            )
                        }
                        is Result.Loading -> {
                            _uiState.value = _uiState.value.copy(isSearching = true)
                        }
                    }
                }
        }
    }

    /**
     * Loads latest manga from all sources
     */
    fun loadLatest() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)
            
            searchManager.getLatestManga()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        error = "Failed to load latest manga: ${e.message}"
                    )
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.value = _uiState.value.copy(
                                searchResults = result.data,
                                isSearching = false,
                                error = null,
                                selectedTab = SearchTab.LATEST
                            )
                        }
                        is Result.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isSearching = false,
                                error = result.message ?: "Failed to load latest manga"
                            )
                        }
                        is Result.Loading -> {
                            _uiState.value = _uiState.value.copy(isSearching = true)
                        }
                    }
                }
        }
    }

    /**
     * Loads popular manga from all sources
     */
    fun loadPopular() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)
            
            searchManager.getPopularManga()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        error = "Failed to load popular manga: ${e.message}"
                    )
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.value = _uiState.value.copy(
                                searchResults = result.data,
                                isSearching = false,
                                error = null,
                                selectedTab = SearchTab.POPULAR
                            )
                        }
                        is Result.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isSearching = false,
                                error = result.message ?: "Failed to load popular manga"
                            )
                        }
                        is Result.Loading -> {
                            _uiState.value = _uiState.value.copy(isSearching = true)
                        }
                    }
                }
        }
    }

    /**
     * Updates search filters
     */
    fun updateFilters(filters: SearchFilters) {
        _uiState.value = _uiState.value.copy(searchFilters = filters)
    }

    /**
     * Toggles filter visibility
     */
    fun toggleFilters() {
        _uiState.value = _uiState.value.copy(showFilters = !_uiState.value.showFilters)
    }

    /**
     * Selects a tab and loads appropriate content
     */
    fun selectTab(tab: SearchTab) {
        when (tab) {
            SearchTab.SEARCH -> {
                // Don't auto-search, wait for user input
                _uiState.value = _uiState.value.copy(selectedTab = tab)
            }
            SearchTab.LATEST -> loadLatest()
            SearchTab.POPULAR -> loadPopular()
        }
    }

    /**
     * Clears any error messages
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Cancels ongoing search
     */
    fun cancelSearch() {
        viewModelScope.launch {
            searchManager.cancelSearch()
            _uiState.value = _uiState.value.copy(isSearching = false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            searchManager.cancelSearch()
        }
    }
}