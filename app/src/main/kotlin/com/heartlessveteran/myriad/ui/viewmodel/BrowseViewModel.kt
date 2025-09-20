package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.usecase.GetLatestMangaUseCase
import com.heartlessveteran.myriad.domain.usecase.SearchMangaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class BrowseUiState(
    val isLoading: Boolean = true,
    val manga: List<Manga> = emptyList(),
    val error: String? = null,
    val page: Int = 1,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
)

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val getLatestMangaUseCase: GetLatestMangaUseCase,
    private val searchMangaUseCase: SearchMangaUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BrowseUiState())
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    init {
        loadLatestManga()
    }

    /**
     * Loads the latest manga (page 1) and updates the browse UI state.
     *
     * Prepares the UI for a non-search load (clears search query and marks not searching), sets loading,
     * then collects the flow returned by getLatestMangaUseCase(1) in the ViewModel scope. While collecting:
     * - On Success: sets isLoading = false, replaces the manga list, clears error, and sets page = 1.
     * - On Error: sets isLoading = false and updates the error message with the result message, the exception
     *   message, or a default fallback.
     * - On Loading: keeps isLoading = true and clears any error.
     *
     * This function has the side effect of updating the internal _uiState and launching a coroutine in viewModelScope.
     */
    fun loadLatestManga() {
        _uiState.update { it.copy(isLoading = true, error = null, searchQuery = "", isSearching = false) }
        getLatestMangaUseCase(1)
            .onEach { result ->
                _uiState.update { currentState ->
                    when (result) {
                        is Result.Success -> {
                            currentState.copy(isLoading = false, manga = result.data, error = null, page = 1)
                        }
                        is Result.Error -> {
                            currentState.copy(
                                isLoading = false,
                                error = result.message ?: result.exception.message ?: "An unexpected error occurred",
                            )
                        }
                        is Result.Loading -> {
                            currentState.copy(isLoading = true, error = null)
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    /**
     * Performs a manga search for the given query and updates the browse UI state with the results.
     *
     * If the query is blank this delegates to [loadLatestManga]. Otherwise it starts a search (page 1),
     * sets the UI into a searching/loading state, and collects results from [searchMangaUseCase] in
     * the ViewModel scope. On success it replaces the displayed manga and resets paging; on error it
     * sets a human-readable error message (preferring the result message, then the exception message,
     * then a default); while loading it keeps the UI in a loading state.
     *
     * @param query The search string; a blank value will reload the latest manga instead of searching.
     */
    fun searchManga(query: String) {
        if (query.isBlank()) {
            loadLatestManga()
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null, searchQuery = query, isSearching = true) }
        searchMangaUseCase(query, 1)
            .onEach { result ->
                _uiState.update { currentState ->
                    when (result) {
                        is Result.Success -> {
                            currentState.copy(isLoading = false, manga = result.data, error = null, page = 1)
                        }
                        is Result.Error -> {
                            currentState.copy(
                                isLoading = false,
                                error = result.message ?: result.exception.message ?: "An unexpected error occurred",
                            )
                        }
                        is Result.Loading -> {
                            currentState.copy(isLoading = true, error = null)
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    fun retry() {
        if (uiState.value.isSearching && uiState.value.searchQuery.isNotBlank()) {
            searchManga(uiState.value.searchQuery)
        } else {
            loadLatestManga()
        }
    }
}
