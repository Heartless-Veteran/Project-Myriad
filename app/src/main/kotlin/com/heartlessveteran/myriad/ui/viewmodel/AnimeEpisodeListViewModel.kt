package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.core.domain.entities.Anime
import com.heartlessveteran.myriad.core.domain.entities.AnimeEpisode
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.usecase.GetAnimeDetailsUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetAnimeEpisodesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the anime episode list screen
 */
data class AnimeEpisodeListUiState(
    val anime: Anime? = null,
    val episodes: List<AnimeEpisode> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * Events that can be sent from the UI
 */
sealed class AnimeEpisodeListEvent {
    data class LoadEpisodes(
        val animeId: String,
    ) : AnimeEpisodeListEvent()

    data class EpisodeSelected(
        val episodeId: String,
    ) : AnimeEpisodeListEvent()

    data object Refresh : AnimeEpisodeListEvent()
}

/**
 * One-time events for the UI
 */
sealed class AnimeEpisodeListUiEvent {
    data class NavigateToPlayer(
        val animeId: String,
        val episodeId: String,
    ) : AnimeEpisodeListUiEvent()

    data class ShowMessage(
        val message: String,
    ) : AnimeEpisodeListUiEvent()
}

/**
 * ViewModel for the anime episode list screen.
 * Handles loading and displaying episodes for a specific anime.
 * Uses manual dependency injection.
 */
class AnimeEpisodeListViewModel(
    private val getAnimeDetailsUseCase: GetAnimeDetailsUseCase,
    private val getAnimeEpisodesUseCase: GetAnimeEpisodesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AnimeEpisodeListUiState())
    val uiState: StateFlow<AnimeEpisodeListUiState> = _uiState.asStateFlow()

    /**
     * Handles events from the UI
     */
    fun onEvent(event: AnimeEpisodeListEvent) {
        when (event) {
            is AnimeEpisodeListEvent.LoadEpisodes -> {
                loadEpisodes(event.animeId)
            }
            is AnimeEpisodeListEvent.EpisodeSelected -> {
                // Handle episode selection
                // This could navigate to player or show additional options
            }
            AnimeEpisodeListEvent.Refresh -> {
                val currentAnime = _uiState.value.anime
                if (currentAnime != null) {
                    loadEpisodes(currentAnime.id)
                }
            }
        }
    }

    private fun loadEpisodes(animeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Load anime details first
                when (val animeResult = getAnimeDetailsUseCase(animeId)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(anime = animeResult.data)
                    }
                    is Result.Error -> {
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                errorMessage = animeResult.message ?: "Failed to load anime details",
                            )
                        return@launch
                    }
                    is Result.Loading -> {
                        // Continue loading
                    }
                }

                // Load episodes
                getAnimeEpisodesUseCase(animeId).collect { episodes ->
                    _uiState.value =
                        _uiState.value.copy(
                            episodes = episodes.sortedBy { it.episodeNumber },
                            isLoading = false,
                        )
                }
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load episodes: ${e.message}",
                    )
            }
        }
    }
}
