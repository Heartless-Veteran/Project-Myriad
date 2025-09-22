package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.core.domain.entities.Anime
import com.heartlessveteran.myriad.core.domain.entities.AnimeEpisode
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.usecase.GetAnimeDetailsUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetAnimeEpisodesUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetNextUnwatchedEpisodeUseCase
import com.heartlessveteran.myriad.core.domain.usecase.UpdateEpisodeProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the anime player screen
 */
data class AnimePlayerUiState(
    val currentAnime: Anime? = null,
    val currentEpisode: AnimeEpisode? = null,
    val episodes: List<AnimeEpisode> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val hasNextEpisode: Boolean = false,
    val hasPreviousEpisode: Boolean = false,
)

/**
 * Events that can be sent from the UI
 */
sealed class AnimePlayerEvent {
    data class LoadEpisode(
        val animeId: String,
        val episodeId: String,
    ) : AnimePlayerEvent()

    data class UpdateProgress(
        val episodeId: String,
        val position: Long,
    ) : AnimePlayerEvent()

    data class MarkEpisodeWatched(
        val episodeId: String,
    ) : AnimePlayerEvent()

    data object PlayPause : AnimePlayerEvent()

    data object NextEpisode : AnimePlayerEvent()

    data object PreviousEpisode : AnimePlayerEvent()

    data class SeekTo(
        val position: Long,
    ) : AnimePlayerEvent()

    data object ToggleFullscreen : AnimePlayerEvent()
}

/**
 * One-time events for the UI
 */
sealed class AnimePlayerUiEvent {
    data class ShowMessage(
        val message: String,
    ) : AnimePlayerUiEvent()

    data class NavigateToEpisode(
        val episodeId: String,
    ) : AnimePlayerUiEvent()

    data object ExitFullscreen : AnimePlayerUiEvent()

    data object EnterFullscreen : AnimePlayerUiEvent()
}

/**
 * ViewModel for the anime player screen.
 * Handles video playback state, episode management, and progress tracking.
 * Uses manual dependency injection.
 */
class AnimePlayerViewModel(
    private val getAnimeDetailsUseCase: GetAnimeDetailsUseCase,
    private val getAnimeEpisodesUseCase: GetAnimeEpisodesUseCase,
    private val updateEpisodeProgressUseCase: UpdateEpisodeProgressUseCase,
    private val getNextUnwatchedEpisodeUseCase: GetNextUnwatchedEpisodeUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AnimePlayerUiState())
    val uiState: StateFlow<AnimePlayerUiState> = _uiState.asStateFlow()

    /**
     * Handles events from the UI
     */
    fun onEvent(event: AnimePlayerEvent) {
        when (event) {
            is AnimePlayerEvent.LoadEpisode -> {
                loadEpisode(event.animeId, event.episodeId)
            }
            is AnimePlayerEvent.UpdateProgress -> {
                updateProgress(event.episodeId, event.position)
            }
            is AnimePlayerEvent.MarkEpisodeWatched -> {
                markEpisodeWatched(event.episodeId)
            }
            AnimePlayerEvent.PlayPause -> {
                togglePlayPause()
            }
            AnimePlayerEvent.NextEpisode -> {
                playNextEpisode()
            }
            AnimePlayerEvent.PreviousEpisode -> {
                playPreviousEpisode()
            }
            is AnimePlayerEvent.SeekTo -> {
                seekTo(event.position)
            }
            AnimePlayerEvent.ToggleFullscreen -> {
                toggleFullscreen()
            }
        }
    }

    private fun loadEpisode(animeId: String, episodeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Load anime details
                when (val animeResult = getAnimeDetailsUseCase(animeId)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(currentAnime = animeResult.data)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = animeResult.message ?: "Failed to load anime"
                        )
                        return@launch
                    }
                    is Result.Loading -> {
                        // Continue loading
                    }
                }

                // Load episodes
                getAnimeEpisodesUseCase(animeId).collect { episodes ->
                    val currentEpisode = episodes.find { it.id == episodeId }
                    if (currentEpisode != null) {
                        val currentIndex = episodes.indexOf(currentEpisode)
                        _uiState.value = _uiState.value.copy(
                            currentEpisode = currentEpisode,
                            episodes = episodes,
                            isLoading = false,
                            hasNextEpisode = currentIndex < episodes.size - 1,
                            hasPreviousEpisode = currentIndex > 0,
                            currentPosition = currentEpisode.watchProgress,
                            duration = currentEpisode.duration
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Episode not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load episode: ${e.message}"
                )
            }
        }
    }

    private fun updateProgress(episodeId: String, position: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentPosition = position)
            
            // Update progress in repository
            val isWatched = position >= (_uiState.value.duration * 0.9) // 90% watched = completed
            updateEpisodeProgressUseCase(episodeId, isWatched, position)
        }
    }

    private fun markEpisodeWatched(episodeId: String) {
        viewModelScope.launch {
            val duration = _uiState.value.duration
            updateEpisodeProgressUseCase(episodeId, true, duration)
            
            // Update local state
            val updatedEpisodes = _uiState.value.episodes.map { episode ->
                if (episode.id == episodeId) {
                    episode.copy(isWatched = true, watchProgress = duration)
                } else {
                    episode
                }
            }
            
            _uiState.value = _uiState.value.copy(episodes = updatedEpisodes)
        }
    }

    private fun togglePlayPause() {
        _uiState.value = _uiState.value.copy(
            isPlaying = !_uiState.value.isPlaying
        )
    }

    private fun playNextEpisode() {
        val currentEpisode = _uiState.value.currentEpisode ?: return
        val episodes = _uiState.value.episodes
        val currentIndex = episodes.indexOf(currentEpisode)
        
        if (currentIndex < episodes.size - 1) {
            val nextEpisode = episodes[currentIndex + 1]
            loadEpisode(currentEpisode.animeId, nextEpisode.id)
        }
    }

    private fun playPreviousEpisode() {
        val currentEpisode = _uiState.value.currentEpisode ?: return
        val episodes = _uiState.value.episodes
        val currentIndex = episodes.indexOf(currentEpisode)
        
        if (currentIndex > 0) {
            val previousEpisode = episodes[currentIndex - 1]
            loadEpisode(currentEpisode.animeId, previousEpisode.id)
        }
    }

    private fun seekTo(position: Long) {
        _uiState.value = _uiState.value.copy(currentPosition = position)
    }

    private fun toggleFullscreen() {
        // This would be handled by the UI layer
        // The ViewModel just tracks the state
    }
}