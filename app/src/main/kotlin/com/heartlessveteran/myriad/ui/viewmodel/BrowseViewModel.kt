package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.domain.model.Manga
import com.heartlessveteran.myriad.domain.usecase.GetLatestMangaUseCase
import com.heartlessveteran.myriad.domain.usecase.SearchMangaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class BrowseUiState(
    val isLoading: Boolean = true,
    val manga: List<Manga> = emptyList(),
    val error: String? = null,
    val page: Int = 1,
    val searchQuery: String = "",
    val isSearching: Boolean = false
)

class BrowseViewModel(
    private val getLatestMangaUseCase: GetLatestMangaUseCase,
    private val searchMangaUseCase: SearchMangaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowseUiState())
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    init {
        loadLatestManga()
    }

    fun loadLatestManga() {
        _uiState.update { it.copy(isLoading = true, error = null, searchQuery = "", isSearching = false) }
        getLatestMangaUseCase(1)
            .onEach { result ->
                _uiState.update { currentState ->
                    result.fold(
                        onSuccess = { mangaList ->
                            currentState.copy(isLoading = false, manga = mangaList, error = null, page = 1)
                        },
                        onFailure = { throwable ->
                            currentState.copy(isLoading = false, error = throwable.localizedMessage ?: "An unexpected error occurred")
                        }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun searchManga(query: String) {
        if (query.isBlank()) {
            loadLatestManga()
            return
        }
        
        _uiState.update { it.copy(isLoading = true, error = null, searchQuery = query, isSearching = true) }
        searchMangaUseCase(query, 1)
            .onEach { result ->
                _uiState.update { currentState ->
                    result.fold(
                        onSuccess = { mangaList ->
                            currentState.copy(isLoading = false, manga = mangaList, error = null, page = 1)
                        },
                        onFailure = { throwable ->
                            currentState.copy(isLoading = false, error = throwable.localizedMessage ?: "An unexpected error occurred")
                        }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun retry() {
        if (uiState.value.isSearching && uiState.value.searchQuery.isNotBlank()) {
            searchManga(uiState.value.searchQuery)
        } else {
            loadLatestManga()
        }
    }
}