package com.heartlessveteran.myriad.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.heartlessveteran.myriad.ui.components.VideoPlayer
import com.heartlessveteran.myriad.ui.components.VideoPlayerConfiguration
import com.heartlessveteran.myriad.ui.theme.MyriadTheme
import kotlinx.coroutines.delay

/**
 * Anime viewing screen with video player.
 *
 * This screen provides:
 * - Video playback for local anime files
 * - Episode navigation and tracking
 * - Playback settings and controls
 * - Progress tracking and resume functionality
 *
 * @see AnimeEpisode Anime episode information
 */
data class AnimeEpisode(
    val id: String,
    val title: String,
    val episodeNumber: Int,
    val videoUri: Uri,
    val duration: Long = 0L,
    val watchedPosition: Long = 0L,
    val isWatched: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeViewingScreen(
    animeTitle: String,
    episode: AnimeEpisode,
    onBackPress: () -> Unit,
    onEpisodeProgress: (Long) -> Unit = {},
    onEpisodeCompleted: () -> Unit = {},
    onNextEpisode: () -> Unit = {},
    onPreviousEpisode: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var playerConfiguration by remember {
        mutableStateOf(
            VideoPlayerConfiguration(
                autoPlay = true,
                resumePosition = episode.watchedPosition,
                showControls = true,
                allowFullScreen = true,
            ),
        )
    }

    var showSettings by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Top app bar (only shown when not in full screen)
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = animeTitle,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                    Text(
                        text = "Episode ${episode.episodeNumber}: ${episode.title}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackPress) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            },
        )

        // Video player
        VideoPlayer(
            videoUri = episode.videoUri,
            title = "$animeTitle - Episode ${episode.episodeNumber}",
            configuration = playerConfiguration,
            onPositionChanged = { position ->
                onEpisodeProgress(position)
            },
            onPlaybackFinished = {
                onEpisodeCompleted()
                // Optionally auto-advance to next episode
                onNextEpisode()
            },
            onError = { errorMessage ->
                error = errorMessage
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
        )

        // Episode navigation controls
        EpisodeNavigationControls(
            episode = episode,
            onPreviousEpisode = onPreviousEpisode,
            onNextEpisode = onNextEpisode,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    // Settings dialog
    if (showSettings) {
        VideoPlayerSettingsDialog(
            configuration = playerConfiguration,
            onConfigurationChanged = { newConfig ->
                playerConfiguration = newConfig
            },
            onDismiss = { showSettings = false },
        )
    }

    // Error snackbar
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error snackbar or dialog
            // For now, just clear the error after showing
            delay(3000)
            error = null
        }
    }
}

@Composable
private fun EpisodeNavigationControls(
    episode: AnimeEpisode,
    onPreviousEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Previous episode button
            OutlinedButton(
                onClick = onPreviousEpisode,
                enabled = episode.episodeNumber > 1,
            ) {
                Text("← Previous")
            }

            // Episode info
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                Text(
                    text = "Episode ${episode.episodeNumber}",
                    style = MaterialTheme.typography.titleSmall,
                )
                if (episode.duration > 0) {
                    val watchedPercentage =
                        (episode.watchedPosition.toFloat() / episode.duration.toFloat() * 100)
                            .toInt()
                    Text(
                        text = "$watchedPercentage% watched",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Next episode button
            Button(onClick = onNextEpisode) {
                Text("Next →")
            }
        }
    }
}

@Composable
private fun VideoPlayerSettingsDialog(
    configuration: VideoPlayerConfiguration,
    onConfigurationChanged: (VideoPlayerConfiguration) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Video Player Settings") },
        text = {
            Column {
                // Auto-play setting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text("Auto-play episodes")
                    Switch(
                        checked = configuration.autoPlay,
                        onCheckedChange = { autoPlay ->
                            onConfigurationChanged(configuration.copy(autoPlay = autoPlay))
                        },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Playback speed
                Text("Playback Speed: ${configuration.playbackSpeed}x")
                Slider(
                    value = configuration.playbackSpeed,
                    onValueChange = { speed ->
                        onConfigurationChanged(configuration.copy(playbackSpeed = speed))
                    },
                    valueRange = 0.25f..2.0f,
                    steps = 6,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Volume
                Text("Volume: ${(configuration.volume * 100).toInt()}%")
                Slider(
                    value = configuration.volume,
                    onValueChange = { volume ->
                        onConfigurationChanged(configuration.copy(volume = volume))
                    },
                    valueRange = 0f..1f,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun AnimeViewingScreenPreview() {
    MyriadTheme {
        AnimeViewingScreen(
            animeTitle = "Sample Anime Series",
            episode =
                AnimeEpisode(
                    id = "1",
                    title = "The Beginning",
                    episodeNumber = 1,
                    videoUri = Uri.EMPTY,
                    duration = 1440000L, // 24 minutes
                    watchedPosition = 360000L, // 6 minutes watched
                    isWatched = false,
                ),
            onBackPress = {},
            onEpisodeProgress = {},
            onEpisodeCompleted = {},
            onNextEpisode = {},
            onPreviousEpisode = {},
        )
    }
}
