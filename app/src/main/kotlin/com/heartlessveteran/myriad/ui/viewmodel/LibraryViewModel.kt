package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.usecase.AddMangaToLibraryUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetLibraryMangaUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetMangaDetailsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state for the library screen
 */
data class LibraryUiState(
    val manga: List<Manga> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedGenre: String? = null,
)

/**
 * Events that can be sent from the UI
 */
sealed class LibraryEvent {
    data class SearchManga(
        val query: String,
    ) : LibraryEvent()

    data class FilterByGenre(
        val genre: String?,
    ) : LibraryEvent()

    data class AddToLibrary(
        val manga: Manga,
    ) : LibraryEvent()

    data object Refresh : LibraryEvent()

    data object ClearError : LibraryEvent()
}

/**
 * One-time events for the UI
 */
sealed class LibraryUiEvent {
    data class ShowError(
        val message: String,
    ) : LibraryUiEvent()

    data class ShowSuccess(
        val message: String,
    ) : LibraryUiEvent()

    data object NavigateToReader : LibraryUiEvent()
}

/**
 * ViewModel for the library screen.
 * Follows MVVM architecture with Clean Architecture use cases.
 * Exposes UI state via StateFlow and handles events properly.
 * Uses manual dependency injection.
 */
class LibraryViewModel(
    private val getLibraryMangaUseCase: GetLibraryMangaUseCase,
    private val getMangaDetailsUseCase: GetMangaDetailsUseCase,
    private val addMangaToLibraryUseCase: AddMangaToLibraryUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _uiEvents = Channel<LibraryUiEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()

    // StateFlow for library manga with automatic updates
    val libraryManga: StateFlow<List<Manga>> =
        getLibraryMangaUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    init {
        // Update UI state when library manga changes
        viewModelScope.launch {
            libraryManga.collect { manga ->
                _uiState.value =
                    _uiState.value.copy(
                        manga = filterManga(manga, _uiState.value.searchQuery, _uiState.value.selectedGenre),
                        isLoading = false,
                    )
            }
        }
    }

    /**
     * Handles events from the UI
     */
    fun onEvent(event: LibraryEvent) {
        when (event) {
            is LibraryEvent.SearchManga -> {
                _uiState.value = _uiState.value.copy(searchQuery = event.query)
                applyFilters()
            }
            is LibraryEvent.FilterByGenre -> {
                _uiState.value = _uiState.value.copy(selectedGenre = event.genre)
                applyFilters()
            }
            is LibraryEvent.AddToLibrary -> {
                addMangaToLibrary(event.manga)
            }
            LibraryEvent.Refresh -> {
                refreshLibrary()
            }
            LibraryEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
        }
    }

    /**
     * Gets manga details by ID
     */
    fun getMangaDetails(mangaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = getMangaDetailsUseCase(mangaId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    // Handle success - could navigate to details screen
                }
                is Result.Error -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Failed to get manga details",
                        )
                    _uiEvents.trySend(LibraryUiEvent.ShowError(result.message ?: "Unknown error"))
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun addMangaToLibrary(manga: Manga) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = addMangaToLibraryUseCase(manga)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _uiEvents.trySend(LibraryUiEvent.ShowSuccess("Added to library"))
                }
                is Result.Error -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Failed to add manga to library",
                        )
                    _uiEvents.trySend(LibraryUiEvent.ShowError(result.message ?: "Unknown error"))
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun refreshLibrary() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        // Library data is automatically refreshed via Flow
        // This could trigger a refresh from remote sources if needed
    }

    private fun applyFilters() {
        val currentManga = libraryManga.value
        val filtered = filterManga(currentManga, _uiState.value.searchQuery, _uiState.value.selectedGenre)
        _uiState.value = _uiState.value.copy(manga = filtered)
    }

    private fun filterManga(
        manga: List<Manga>,
        query: String,
        genre: String?,
    ): List<Manga> {
        var filtered = manga

        if (query.isNotBlank()) {
            filtered = filtered.filter { it.title.contains(query, ignoreCase = true) }
        }

        if (genre != null) {
            filtered = filtered.filter { it.genres.contains(genre) }
        }

        return filtered
    }
}
