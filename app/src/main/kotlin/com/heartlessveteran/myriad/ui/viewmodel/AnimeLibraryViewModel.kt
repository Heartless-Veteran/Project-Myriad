package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.core.domain.entities.Anime
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.usecase.AddAnimeToLibraryUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetAnimeDetailsUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetLibraryAnimeUseCase
import com.heartlessveteran.myriad.core.domain.usecase.SearchLibraryAnimeUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state for the anime library screen
 */
data class AnimeLibraryUiState(
    val anime: List<Anime> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedGenre: String? = null,
)

/**
 * Events that can be sent from the UI
 */
sealed class AnimeLibraryEvent {
    data class SearchQueryChanged(
        val query: String,
    ) : AnimeLibraryEvent()

    data class FilterByGenre(
        val genre: String?,
    ) : AnimeLibraryEvent()

    data class AddToLibrary(
        val anime: Anime,
    ) : AnimeLibraryEvent()

    data class AnimeClicked(
        val anime: Anime,
    ) : AnimeLibraryEvent()

    data object Refresh : AnimeLibraryEvent()

    data object ClearError : AnimeLibraryEvent()
}

/**
 * One-time events for the UI
 */
sealed class AnimeLibraryUiEvent {
    data class ShowSnackbar(
        val message: String,
    ) : AnimeLibraryUiEvent()

    data class NavigateToAnimeDetails(
        val animeId: String,
    ) : AnimeLibraryUiEvent()

    data class NavigateToPlayer(
        val animeId: String,
        val episodeId: String,
    ) : AnimeLibraryUiEvent()
}

/**
 * ViewModel for the anime library screen.
 * Follows MVVM architecture with Clean Architecture use cases.
 * Exposes UI state via StateFlow and handles events properly.
 * Uses manual dependency injection.
 */
class AnimeLibraryViewModel(
    private val getLibraryAnimeUseCase: GetLibraryAnimeUseCase,
    private val getAnimeDetailsUseCase: GetAnimeDetailsUseCase,
    private val addAnimeToLibraryUseCase: AddAnimeToLibraryUseCase,
    private val searchLibraryAnimeUseCase: SearchLibraryAnimeUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AnimeLibraryUiState())
    val uiState: StateFlow<AnimeLibraryUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<AnimeLibraryUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // StateFlow for library anime with automatic updates
    val libraryAnime: StateFlow<List<Anime>> =
        getLibraryAnimeUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    init {
        // Update UI state when library anime changes
        viewModelScope.launch {
            libraryAnime.collect { anime ->
                _uiState.value =
                    _uiState.value.copy(
                        anime = filterAnime(anime, _uiState.value.searchQuery, _uiState.value.selectedGenre),
                        isLoading = false,
                    )
            }
        }
    }

    /**
     * Handles events from the UI
     */
    fun onEvent(event: AnimeLibraryEvent) {
        when (event) {
            is AnimeLibraryEvent.SearchQueryChanged -> {
                _uiState.value = _uiState.value.copy(searchQuery = event.query)
                applyFilters()
            }
            is AnimeLibraryEvent.FilterByGenre -> {
                _uiState.value = _uiState.value.copy(selectedGenre = event.genre)
                applyFilters()
            }
            is AnimeLibraryEvent.AddToLibrary -> {
                addAnimeToLibrary(event.anime)
            }
            is AnimeLibraryEvent.AnimeClicked -> {
                handleAnimeClick(event.anime)
            }
            AnimeLibraryEvent.Refresh -> {
                refreshLibrary()
            }
            AnimeLibraryEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
        }
    }

    /**
     * Gets anime details by ID
     */
    fun getAnimeDetails(animeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = getAnimeDetailsUseCase(animeId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _uiEvent.trySend(AnimeLibraryUiEvent.NavigateToAnimeDetails(animeId))
                }
                is Result.Error -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Failed to get anime details",
                        )
                    _uiEvent.trySend(AnimeLibraryUiEvent.ShowSnackbar(result.message ?: "Unknown error"))
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun handleAnimeClick(anime: Anime) {
        // Navigate to anime details or player based on context
        // For now, just show anime details
        getAnimeDetails(anime.id)
    }

    private fun addAnimeToLibrary(anime: Anime) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = addAnimeToLibraryUseCase(anime)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _uiEvent.trySend(AnimeLibraryUiEvent.ShowSnackbar("Added to library"))
                }
                is Result.Error -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Failed to add anime to library",
                        )
                    _uiEvent.trySend(AnimeLibraryUiEvent.ShowSnackbar(result.message ?: "Unknown error"))
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
        val currentAnime = libraryAnime.value
        val filtered = filterAnime(currentAnime, _uiState.value.searchQuery, _uiState.value.selectedGenre)
        _uiState.value = _uiState.value.copy(anime = filtered)
    }

    private fun filterAnime(
        anime: List<Anime>,
        query: String,
        genre: String?,
    ): List<Anime> {
        var filtered = anime

        if (query.isNotBlank()) {
            filtered =
                filtered.filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.alternativeTitles.any { title -> title.contains(query, ignoreCase = true) } ||
                        it.description.contains(query, ignoreCase = true)
                }
        }

        if (genre != null) {
            filtered = filtered.filter { it.genres.contains(genre) }
        }

        return filtered
    }
}
