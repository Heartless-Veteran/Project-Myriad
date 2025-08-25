package com.projectmyriad.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectmyriad.domain.entities.Manga
import com.projectmyriad.domain.usecases.MangaLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for manga library operations.
 * Manages UI state and coordinates with domain use cases.
 */
@HiltViewModel
class MangaLibraryViewModel @Inject constructor(
    private val mangaLibraryUseCase: MangaLibraryUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MangaLibraryUiState())
    val uiState: StateFlow<MangaLibraryUiState> = _uiState.asStateFlow()
    
    init {
        loadManga()
    }
    
    fun loadManga() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            mangaLibraryUseCase.getAllManga()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error occurred"
                    )
                }
                .collect { mangaList ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        mangaList = mangaList,
                        error = null
                    )
                }
        }
    }
    
    fun searchManga(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(searchQuery = query, isLoading = true)
            
            mangaLibraryUseCase.searchManga(query)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Search failed"
                    )
                }
                .collect { results ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        mangaList = results
                    )
                }
        }
    }
    
    fun toggleFavorite(mangaId: String) {
        viewModelScope.launch {
            mangaLibraryUseCase.toggleFavorite(mangaId)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to update favorite"
                    )
                }
        }
    }
    
    fun updateReadingProgress(mangaId: String, progress: Float) {
        viewModelScope.launch {
            mangaLibraryUseCase.updateReadingProgress(mangaId, progress)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to update progress"
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun importManga(filePath: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            
            mangaLibraryUseCase.importManga(filePath)
                .onSuccess { 
                    _uiState.value = _uiState.value.copy(isImporting = false)
                    loadManga() // Refresh the list
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        error = error.message ?: "Failed to import manga"
                    )
                }
        }
    }
}

/**
 * UI state for manga library screen
 */
data class MangaLibraryUiState(
    val isLoading: Boolean = false,
    val isImporting: Boolean = false,
    val mangaList: List<Manga> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)