package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Simple Global Search ViewModel for Phase 2-3 integration
 */
class SimpleGlobalSearchViewModel : ViewModel() {
    
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun search(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // Simulate search results
            _searchResults.value = listOf(
                SearchResult("1", "Sample Manga Result", "Manga", "Sample description"),
                SearchResult("2", "Sample Anime Result", "Anime", "Sample description")
            )
            _isLoading.value = false
        }
    }
}

data class SearchResult(
    val id: String,
    val title: String,
    val type: String,
    val description: String
)