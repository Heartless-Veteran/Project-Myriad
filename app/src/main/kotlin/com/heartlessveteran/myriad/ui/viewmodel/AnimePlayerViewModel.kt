package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.core.domain.entities.Anime
import com.heartlessveteran.myriad.core.domain.entities.AnimeEpisode
import com.heartlessveteran.myriad.core.domain.entities.AudioTrack
import com.heartlessveteran.myriad.core.domain.entities.Chapter
import com.heartlessveteran.myriad.core.domain.entities.ChapterType
import com.heartlessveteran.myriad.core.domain.entities.SubtitleTrack
import com.heartlessveteran.myriad.core.domain.entities.VideoPlaybackSettings
import com.heartlessveteran.myriad.core.domain.entities.VideoQuality
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.usecase.GetAnimeDetailsUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetAnimeEpisodesUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetNextUnwatchedEpisodeUseCase
import com.heartlessveteran.myriad.core.domain.usecase.UpdateEpisodeProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the anime player screen with enhanced video playback features
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
    // Enhanced video playback features
    val playbackSettings: VideoPlaybackSettings = VideoPlaybackSettings(),
    val availableAudioTracks: List<AudioTrack> = emptyList(),
    val availableSubtitleTracks: List<SubtitleTrack> = emptyList(),
    val selectedAudioTrackId: Int? = null,
    val selectedSubtitleTrackIds: List<Int> = emptyList(),
    val currentPlaybackSpeed: Float = 1.0f,
    val availableQualities: List<VideoQuality> = emptyList(),
    val selectedQuality: VideoQuality? = null,
    val chapters: List<Chapter> = emptyList(),
    val currentChapter: Chapter? = null,
    val isFullscreen: Boolean = false,
    val showPlayerControls: Boolean = true,
    val volume: Float = 1.0f,
    val brightness: Float = 1.0f,
    // Frame rate detection
    val detectedFrameRate: Float? = null,
    val isFrameRateMatched: Boolean = false,
    // Chapter navigation
    val canSkipIntro: Boolean = false,
    val canSkipOutro: Boolean = false,
    val introChapter: Chapter? = null,
    val outroChapter: Chapter? = null,
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

    // Enhanced video playback events
    data class ChangePlaybackSpeed(
        val speed: Float,
    ) : AnimePlayerEvent()

    data class SelectAudioTrack(
        val trackId: Int,
    ) : AnimePlayerEvent()

    data class SelectSubtitleTrack(
        val trackId: Int,
        val enabled: Boolean = true,
    ) : AnimePlayerEvent()

    data class SelectVideoQuality(
        val quality: VideoQuality,
    ) : AnimePlayerEvent()

    data class SeekToChapter(
        val chapter: Chapter,
    ) : AnimePlayerEvent()

    data object SkipIntro : AnimePlayerEvent()

    data object SkipOutro : AnimePlayerEvent()

    data class AdjustVolume(
        val volume: Float,
    ) : AnimePlayerEvent()

    data class AdjustBrightness(
        val brightness: Float,
    ) : AnimePlayerEvent()

    data class UpdateSubtitleStyle(
        val settings: VideoPlaybackSettings,
    ) : AnimePlayerEvent()

    data object TogglePlayerControls : AnimePlayerEvent()

    data object DetectFrameRate : AnimePlayerEvent()

    data class LoadChapters(
        val episodeId: String,
    ) : AnimePlayerEvent()
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

    // Enhanced video playback UI events
    data class ShowPlaybackSpeedChanged(
        val speed: Float,
    ) : AnimePlayerUiEvent()

    data class ShowAudioTrackChanged(
        val track: AudioTrack,
    ) : AnimePlayerUiEvent()

    data class ShowSubtitleTrackChanged(
        val track: SubtitleTrack,
        val enabled: Boolean,
    ) : AnimePlayerUiEvent()

    data class ShowQualityChanged(
        val quality: VideoQuality,
    ) : AnimePlayerUiEvent()

    data class ShowChapterInfo(
        val chapter: Chapter,
    ) : AnimePlayerUiEvent()

    data object ShowIntroSkipped : AnimePlayerUiEvent()

    data object ShowOutroSkipped : AnimePlayerUiEvent()

    data class ShowFrameRateDetected(
        val frameRate: Float,
    ) : AnimePlayerUiEvent()

    data object ShowPlayerControlsToggled : AnimePlayerUiEvent()
}

/**
 * ViewModel for the anime player screen.
 * Handles video playback state, episode management, and progress tracking.
 * Uses Hilt dependency injection.
 */
@HiltViewModel
class AnimePlayerViewModel
    @Inject
    constructor(
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
                is AnimePlayerEvent.ChangePlaybackSpeed -> {
                    changePlaybackSpeed(event.speed)
                }
                is AnimePlayerEvent.SelectAudioTrack -> {
                    selectAudioTrack(event.trackId)
                }
                is AnimePlayerEvent.SelectSubtitleTrack -> {
                    selectSubtitleTrack(event.trackId, event.enabled)
                }
                is AnimePlayerEvent.SelectVideoQuality -> {
                    selectVideoQuality(event.quality)
                }
                is AnimePlayerEvent.SeekToChapter -> {
                    seekToChapter(event.chapter)
                }
                AnimePlayerEvent.SkipIntro -> {
                    skipIntro()
                }
                AnimePlayerEvent.SkipOutro -> {
                    skipOutro()
                }
                is AnimePlayerEvent.AdjustVolume -> {
                    adjustVolume(event.volume)
                }
                is AnimePlayerEvent.AdjustBrightness -> {
                    adjustBrightness(event.brightness)
                }
                is AnimePlayerEvent.UpdateSubtitleStyle -> {
                    updateSubtitleStyle(event.settings)
                }
                AnimePlayerEvent.TogglePlayerControls -> {
                    togglePlayerControls()
                }
                AnimePlayerEvent.DetectFrameRate -> {
                    detectFrameRate()
                }
                is AnimePlayerEvent.LoadChapters -> {
                    loadChapters(event.episodeId)
                }
            }
        }

        private fun loadEpisode(
            animeId: String,
            episodeId: String,
        ) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                try {
                    // Load anime details
                    when (val animeResult = getAnimeDetailsUseCase(animeId)) {
                        is Result.Success -> {
                            _uiState.value = _uiState.value.copy(currentAnime = animeResult.data)
                        }
                        is Result.Error -> {
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = animeResult.message ?: "Failed to load anime",
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
                            _uiState.value =
                                _uiState.value.copy(
                                    currentEpisode = currentEpisode,
                                    episodes = episodes,
                                    isLoading = false,
                                    hasNextEpisode = currentIndex < episodes.size - 1,
                                    hasPreviousEpisode = currentIndex > 0,
                                    currentPosition = currentEpisode.watchProgress,
                                    duration = currentEpisode.duration,
                                    // Initialize enhanced features with mock data
                                    availableAudioTracks = generateMockAudioTracks(),
                                    availableSubtitleTracks = generateMockSubtitleTracks(),
                                    selectedAudioTrackId = 0, // Default to first audio track
                                    selectedSubtitleTrackIds = listOf(0), // Default to first subtitle track
                                    currentPlaybackSpeed = 1.0f,
                                    availableQualities = generateMockQualities(),
                                    selectedQuality = generateMockQualities().first(),
                                    chapters = emptyList(), // Will be loaded separately
                                    volume = 1.0f,
                                    brightness = 1.0f,
                                )

                            // Load chapters for this episode
                            loadChapters(episodeId)

                            // Detect frame rate
                            detectFrameRate()
                        } else {
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Episode not found",
                                )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to load episode: ${e.message}",
                        )
                }
            }
        }

        private fun updateProgress(
            episodeId: String,
            position: Long,
        ) {
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
                val updatedEpisodes =
                    _uiState.value.episodes.map { episode ->
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
            _uiState.value =
                _uiState.value.copy(
                    isPlaying = !_uiState.value.isPlaying,
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
            _uiState.value =
                _uiState.value.copy(
                    isFullscreen = !_uiState.value.isFullscreen,
                )
        }

        // Enhanced video playback feature implementations
        private fun changePlaybackSpeed(speed: Float) {
            val settings = _uiState.value.playbackSettings
            if (speed in settings.availableSpeedOptions) {
                _uiState.value = _uiState.value.copy(currentPlaybackSpeed = speed)
                // In a real implementation, this would communicate with ExoPlayer
            }
        }

        private fun selectAudioTrack(trackId: Int) {
            val availableTracks = _uiState.value.availableAudioTracks
            val selectedTrack = availableTracks.find { it.trackId == trackId }

            if (selectedTrack != null) {
                _uiState.value = _uiState.value.copy(selectedAudioTrackId = trackId)
                // In a real implementation, this would update ExoPlayer's track selection
            }
        }

        private fun selectSubtitleTrack(
            trackId: Int,
            enabled: Boolean,
        ) {
            val currentTracks = _uiState.value.selectedSubtitleTrackIds.toMutableList()

            if (enabled && trackId !in currentTracks) {
                // Enable subtitle track
                currentTracks.add(trackId)
            } else if (!enabled) {
                // Disable subtitle track
                currentTracks.remove(trackId)
            }

            _uiState.value = _uiState.value.copy(selectedSubtitleTrackIds = currentTracks)
            // In a real implementation, this would update ExoPlayer's subtitle tracks
        }

        private fun selectVideoQuality(quality: VideoQuality) {
            _uiState.value = _uiState.value.copy(selectedQuality = quality)
            // In a real implementation, this would change the video quality in ExoPlayer
        }

        private fun seekToChapter(chapter: Chapter) {
            _uiState.value =
                _uiState.value.copy(
                    currentPosition = chapter.startTimeMs,
                    currentChapter = chapter,
                )
            // In a real implementation, this would seek ExoPlayer to the chapter start time
        }

        private fun skipIntro() {
            val introChapter = _uiState.value.introChapter
            if (introChapter != null) {
                _uiState.value =
                    _uiState.value.copy(
                        currentPosition = introChapter.endTimeMs,
                        canSkipIntro = false,
                    )
                // In a real implementation, this would seek ExoPlayer past the intro
            }
        }

        private fun skipOutro() {
            val outroChapter = _uiState.value.outroChapter
            if (outroChapter != null) {
                // Skip to next episode or end of video
                if (_uiState.value.hasNextEpisode) {
                    playNextEpisode()
                } else {
                    _uiState.value =
                        _uiState.value.copy(
                            currentPosition = _uiState.value.duration,
                            isPlaying = false,
                        )
                }
            }
        }

        private fun adjustVolume(volume: Float) {
            val clampedVolume = volume.coerceIn(0.0f, 1.0f)
            _uiState.value = _uiState.value.copy(volume = clampedVolume)
            // In a real implementation, this would adjust ExoPlayer's volume
        }

        private fun adjustBrightness(brightness: Float) {
            val clampedBrightness = brightness.coerceIn(0.1f, 1.0f)
            _uiState.value = _uiState.value.copy(brightness = clampedBrightness)
            // In a real implementation, this would adjust the screen brightness
        }

        private fun updateSubtitleStyle(settings: VideoPlaybackSettings) {
            _uiState.value = _uiState.value.copy(playbackSettings = settings)
            // In a real implementation, this would update ExoPlayer's subtitle style
        }

        private fun togglePlayerControls() {
            _uiState.value =
                _uiState.value.copy(
                    showPlayerControls = !_uiState.value.showPlayerControls,
                )
        }

        private fun detectFrameRate() {
            // Mock frame rate detection for anime content (typically 24fps)
            val detectedFrameRate = 24.0f
            val shouldMatch = _uiState.value.playbackSettings.enableFrameRateMatching

            _uiState.value =
                _uiState.value.copy(
                    detectedFrameRate = detectedFrameRate,
                    isFrameRateMatched =
                        shouldMatch && detectedFrameRate == _uiState.value.playbackSettings.preferredFrameRate,
                )

            // In a real implementation, this would analyze the video stream for frame rate
        }

        private fun loadChapters(episodeId: String) {
            viewModelScope.launch {
                // Mock chapter data for demonstration
                val mockChapters = generateMockChapters()
                val introChapter = mockChapters.find { it.type == ChapterType.INTRO }
                val outroChapter = mockChapters.find { it.type == ChapterType.OUTRO }

                _uiState.value =
                    _uiState.value.copy(
                        chapters = mockChapters,
                        introChapter = introChapter,
                        outroChapter = outroChapter,
                        canSkipIntro = introChapter != null,
                        canSkipOutro = outroChapter != null,
                    )

                // In a real implementation, this would load chapters from metadata or AI detection
            }
        }

        private fun generateMockAudioTracks(): List<AudioTrack> =
            listOf(
                AudioTrack(
                    trackId = 0,
                    language = "Japanese",
                    languageCode = "ja",
                    name = "Japanese (Original)",
                    isDefault = true,
                    channels = 2,
                    bitrate = 128000,
                ),
                AudioTrack(
                    trackId = 1,
                    language = "English",
                    languageCode = "en",
                    name = "English (Dub)",
                    isDefault = false,
                    channels = 2,
                    bitrate = 128000,
                ),
            )

        private fun generateMockSubtitleTracks(): List<SubtitleTrack> =
            listOf(
                SubtitleTrack(
                    trackId = 0,
                    language = "English",
                    languageCode = "en",
                    name = "English (Full)",
                    isDefault = true,
                    isForced = false,
                    isKaraoke = false,
                ),
                SubtitleTrack(
                    trackId = 1,
                    language = "English",
                    languageCode = "en",
                    name = "English (Signs & Songs)",
                    isDefault = false,
                    isForced = true,
                    isKaraoke = false,
                ),
                SubtitleTrack(
                    trackId = 2,
                    language = "English",
                    languageCode = "en",
                    name = "English (Karaoke)",
                    isDefault = false,
                    isForced = false,
                    isKaraoke = true,
                ),
            )

        private fun generateMockQualities(): List<VideoQuality> =
            listOf(
                VideoQuality(
                    width = 1920,
                    height = 1080,
                    frameRate = 24.0f,
                    bitrate = 8000000L,
                    codec = "H.264",
                ),
                VideoQuality(
                    width = 1280,
                    height = 720,
                    frameRate = 24.0f,
                    bitrate = 4000000L,
                    codec = "H.264",
                ),
                VideoQuality(
                    width = 854,
                    height = 480,
                    frameRate = 24.0f,
                    bitrate = 2000000L,
                    codec = "H.264",
                ),
            )

        private fun generateMockChapters(): List<Chapter> {
            // Mock chapter data for typical anime episode structure
            return listOf(
                Chapter(
                    startTimeMs = 0L,
                    endTimeMs = 90000L, // 1:30 intro
                    title = "Opening",
                    type = ChapterType.INTRO,
                ),
                Chapter(
                    startTimeMs = 90000L,
                    endTimeMs = 1200000L, // 20 minutes of content
                    title = "Episode Content",
                    type = ChapterType.CONTENT,
                ),
                Chapter(
                    startTimeMs = 1200000L,
                    endTimeMs = 1290000L, // 1:30 outro
                    title = "Ending",
                    type = ChapterType.OUTRO,
                ),
                Chapter(
                    startTimeMs = 1290000L,
                    endTimeMs = 1320000L, // 30 seconds preview
                    title = "Next Episode Preview",
                    type = ChapterType.PREVIEW,
                ),
            )
        }
    }
