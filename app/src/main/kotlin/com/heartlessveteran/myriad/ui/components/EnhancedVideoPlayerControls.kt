package com.heartlessveteran.myriad.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heartlessveteran.myriad.core.domain.entities.*
import com.heartlessveteran.myriad.ui.viewmodel.AnimePlayerEvent
import com.heartlessveteran.myriad.ui.viewmodel.AnimePlayerUiState

/**
 * Enhanced video player controls for anime playback with advanced features
 */
@Composable
fun EnhancedVideoPlayerControls(
    uiState: AnimePlayerUiState,
    onEvent: (AnimePlayerEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.showPlayerControls) {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
        ) {
            // Main playback controls
            MainPlaybackControls(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.align(Alignment.Center),
            )

            // Top bar with episode info and settings
            TopControlBar(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.align(Alignment.TopCenter),
            )

            // Bottom bar with progress and additional controls
            BottomControlBar(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.align(Alignment.BottomCenter),
            )

            // Right side controls for quick access
            SideQuickControls(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}

@Composable
private fun MainPlaybackControls(
    uiState: AnimePlayerUiState,
    onEvent: (AnimePlayerEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Previous episode
        IconButton(
            onClick = { onEvent(AnimePlayerEvent.PreviousEpisode) },
            enabled = uiState.hasPreviousEpisode,
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous Episode",
                tint = Color.White,
                modifier = Modifier.size(40.dp),
            )
        }

        // Play/Pause
        IconButton(
            onClick = { onEvent(AnimePlayerEvent.PlayPause) },
        ) {
            Icon(
                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(56.dp),
            )
        }

        // Next episode
        IconButton(
            onClick = { onEvent(AnimePlayerEvent.NextEpisode) },
            enabled = uiState.hasNextEpisode,
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next Episode",
                tint = Color.White,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

// Other composables would be here but simplified for demonstration
@Composable
private fun TopControlBar(
    uiState: AnimePlayerUiState,
    onEvent: (AnimePlayerEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Simplified implementation for now
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = uiState.currentAnime?.title ?: "",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = uiState.currentEpisode?.title ?: "",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (uiState.canSkipIntro) {
                Button(
                    onClick = { onEvent(AnimePlayerEvent.SkipIntro) },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Skip Intro")
                }
            }

            if (uiState.canSkipOutro) {
                Button(
                    onClick = { onEvent(AnimePlayerEvent.SkipOutro) },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                        ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Skip Outro")
                }
            }
        }
    }
}

@Composable
private fun BottomControlBar(
    uiState: AnimePlayerUiState,
    onEvent: (AnimePlayerEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Simplified implementation
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${formatTime(uiState.currentPosition)} / ${formatTime(uiState.duration)}",
            color = Color.White,
            fontSize = 12.sp,
        )

        Text(
            text = "${uiState.currentPlaybackSpeed}x",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
        )

        IconButton(
            onClick = { onEvent(AnimePlayerEvent.ToggleFullscreen) },
        ) {
            Icon(
                imageVector = if (uiState.isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription = if (uiState.isFullscreen) "Exit Fullscreen" else "Enter Fullscreen",
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun SideQuickControls(
    uiState: AnimePlayerUiState,
    onEvent: (AnimePlayerEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Simplified implementation for playback speed control
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Playback Speed",
                    tint = Color.White,
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                uiState.playbackSettings.availableSpeedOptions.forEach { speed ->
                    DropdownMenuItem(
                        text = { Text("${speed}x") },
                        onClick = {
                            onEvent(AnimePlayerEvent.ChangePlaybackSpeed(speed))
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
