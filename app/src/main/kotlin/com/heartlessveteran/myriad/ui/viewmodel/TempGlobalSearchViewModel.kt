package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.heartlessveteran.myriad.core.domain.manager.SearchResult
import com.heartlessveteran.myriad.core.domain.manager.GroupedSearchResults
import com.heartlessveteran.myriad.core.domain.manager.SearchFilters
import com.heartlessveteran.myriad.feature.browser.viewmodel.GlobalSearchUiState
import com.heartlessveteran.myriad.feature.browser.viewmodel.SearchTab

/**
 * Temporary GlobalSearchViewModel for phase compatibility
 * This is a minimal implementation to fix build issues
 */
class TempGlobalSearchViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(
        GlobalSearchUiState(
            query = "",
            searchResults = GroupedSearchResults(emptyMap(), 0, false),
            isSearching = false,
            error = null,
            searchFilters = SearchFilters(""),
            showFilters = false,
            selectedTab = SearchTab.SEARCH
        )
    )
    val uiState: StateFlow<GlobalSearchUiState> = _uiState.asStateFlow()
    
    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }
    
    fun search() {
        // Temporary implementation - no actual search
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun toggleFilters() {
        _uiState.value = _uiState.value.copy(showFilters = !_uiState.value.showFilters)
    }
    
    fun selectTab(tab: SearchTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }
}