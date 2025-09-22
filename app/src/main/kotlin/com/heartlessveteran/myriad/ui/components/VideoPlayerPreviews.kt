package com.heartlessveteran.myriad.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.heartlessveteran.myriad.core.domain.entities.*
import com.heartlessveteran.myriad.ui.viewmodel.AnimePlayerUiState

/**
 * Preview composables for the enhanced video player components
 */
@Preview(
    name = "Enhanced Video Player Controls",
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
)
@Composable
fun EnhancedVideoPlayerControlsPreview() {
    MaterialTheme {
        val mockUiState =
            AnimePlayerUiState(
                currentAnime = null, // Would have anime data
                currentEpisode = null, // Would have episode data
                isPlaying = true,
                currentPosition = 300000L, // 5 minutes
                duration = 1440000L, // 24 minutes
                hasNextEpisode = true,
                hasPreviousEpisode = true,
                currentPlaybackSpeed = 1.0f,
                canSkipIntro = true,
                canSkipOutro = false,
                showPlayerControls = true,
                volume = 0.8f,
                brightness = 0.9f,
                playbackSettings =
                    VideoPlaybackSettings(
                        availableSpeedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f),
                    ),
            )

        Box(modifier = Modifier.size(400.dp, 600.dp)) {
            EnhancedVideoPlayerControls(
                uiState = mockUiState,
                onEvent = { /* Preview - no actions */ },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(
    name = "Video Settings Screen",
    showBackground = true,
    widthDp = 400,
    heightDp = 800,
)
@Composable
fun VideoPlaybackSettingsScreenPreview() {
    MaterialTheme {
        val mockSettings =
            VideoPlaybackSettings(
                preferredAudioLanguage = "ja",
                preferredSubtitleLanguage = "en",
                enableSubtitlesByDefault = true,
                enableFrameRateMatching = true,
                preferredFrameRate = 24.0f,
                defaultPlaybackSpeed = 1.0f,
                enablePitchCorrection = true,
                subtitleFontSize = 16.0f,
                subtitleVerticalPosition = 0.9f,
                subtitleHorizontalAlignment = SubtitleAlignment.CENTER,
                enableChapterNavigation = true,
                enableAutoSkipIntro = false,
                enableAutoSkipOutro = false,
                enableKaraokeSubtitles = true,
            )

        VideoPlaybackSettingsScreen(
            settings = mockSettings,
            onSettingsChange = { /* Preview - no actions */ },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
