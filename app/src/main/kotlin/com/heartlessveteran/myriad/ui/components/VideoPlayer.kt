package com.heartlessveteran.myriad.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

/**
 * Video player component for anime playback.
 *
 * This component provides:
 * - Video playback for .mp4, .mkv, .avi files
 * - Custom playback controls
 * - Progress tracking and seeking
 * - Volume and brightness controls
 * - Full-screen support
 */

/**
 * Video player configuration options
 */
data class VideoPlayerConfiguration(
    val autoPlay: Boolean = false,
    val showControls: Boolean = true,
    val allowFullScreen: Boolean = true,
    val resumePosition: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val brightness: Float = -1f, // -1 means system brightness
)

/**
 * Video player state information
 */
data class VideoPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
)

@Composable
fun VideoPlayer(
    videoUri: Uri,
    title: String,
    configuration: VideoPlayerConfiguration = VideoPlayerConfiguration(),
    onPositionChanged: (Long) -> Unit = {},
    onPlaybackFinished: () -> Unit = {},
    onError: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var playerState by remember { mutableStateOf(VideoPlayerState()) }
    var showControls by remember { mutableStateOf(configuration.showControls) }
    var controlsVisibleTimer by remember { mutableStateOf(0) }
    
    // Create ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                // Set initial configuration
                playWhenReady = configuration.autoPlay
                setMediaItem(MediaItem.fromUri(videoUri))
                if (configuration.resumePosition > 0) {
                    seekTo(configuration.resumePosition)
                }
                prepare()
                
                // Add listener for player events
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        playerState = playerState.copy(
                            isPlaying = isPlaying,
                            isBuffering = playbackState == Player.STATE_BUFFERING,
                            hasError = false
                        )
                        
                        if (playbackState == Player.STATE_ENDED) {
                            onPlaybackFinished()
                        }
                    }
                    
                    override fun onPlayerError(error: PlaybackException) {
                        val errorMsg = error.localizedMessage ?: "Video playback failed"
                        playerState = playerState.copy(
                            hasError = true,
                            errorMessage = errorMsg
                        )
                        onError(errorMsg)
                    }
                })
            }
    }
    
    // Update player state periodically
    LaunchedEffect(playerState.isPlaying) {
        if (playerState.isPlaying) {
            while (true) {
                val currentPosition = exoPlayer.currentPosition
                playerState = playerState.copy(
                    currentPosition = currentPosition,
                    duration = exoPlayer.duration.takeIf { it != androidx.media3.common.C.TIME_UNSET } ?: 0L
                )
                onPositionChanged(currentPosition)
                delay(1000) // Update every second
            }
        }
    }
    
    // Hide controls after timeout
    LaunchedEffect(showControls, controlsVisibleTimer) {
        if (showControls && playerState.isPlaying) {
            delay(3000) // Hide after 3 seconds
            showControls = false
        }
    }
    
    // Cleanup when component is disposed
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                showControls = !showControls
                controlsVisibleTimer++
            }
    ) {
        // Video player view
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false // We'll use custom controls
                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Custom controls overlay
        if (showControls && !playerState.hasError) {
            VideoPlayerControls(
                state = playerState,
                title = title,
                onPlayPause = {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                    controlsVisibleTimer++
                },
                onSeek = { position ->
                    exoPlayer.seekTo(position)
                    controlsVisibleTimer++
                },
                onSpeedChange = { speed ->
                    exoPlayer.setPlaybackSpeed(speed)
                    playerState = playerState.copy(playbackSpeed = speed)
                    controlsVisibleTimer++
                },
                onVolumeChange = { volume ->
                    exoPlayer.volume = volume
                    playerState = playerState.copy(volume = volume)
                    controlsVisibleTimer++
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Error overlay
        if (playerState.hasError) {
            VideoPlayerError(
                message = playerState.errorMessage ?: "Unknown error",
                onRetry = {
                    exoPlayer.prepare()
                    playerState = playerState.copy(hasError = false, errorMessage = null)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Loading indicator
        if (playerState.isBuffering && !playerState.hasError) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun VideoPlayerControls(
    state: VideoPlayerState,
    title: String,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        // Top bar with title
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            )
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Center play/pause button
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        // Bottom controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            )
        ) {
            VideoPlayerBottomControls(
                state = state,
                onSeek = onSeek,
                onSpeedChange = onSpeedChange,
                onVolumeChange = onVolumeChange,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun VideoPlayerBottomControls(
    state: VideoPlayerState,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Progress bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(state.currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                modifier = Modifier.width(48.dp)
            )
            
            Slider(
                value = if (state.duration > 0) {
                    state.currentPosition.toFloat() / state.duration.toFloat()
                } else 0f,
                onValueChange = { progress ->
                    val newPosition = (progress * state.duration).toLong()
                    onSeek(newPosition)
                },
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color.Gray
                )
            )
            
            Text(
                text = formatTime(state.duration),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                modifier = Modifier.width(48.dp)
            )
        }
        
        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed control
            var showSpeedDialog by remember { mutableStateOf(false) }
            OutlinedButton(
                onClick = { showSpeedDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("${state.playbackSpeed}x")
            }
            
            // Volume control
            var showVolumeDialog by remember { mutableStateOf(false) }
            IconButton(onClick = { showVolumeDialog = true }) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Volume",
                    tint = Color.White
                )
            }
            
            // Dialogs for speed and volume
            if (showSpeedDialog) {
                SpeedSelectionDialog(
                    currentSpeed = state.playbackSpeed,
                    onSpeedSelected = { speed ->
                        onSpeedChange(speed)
                        showSpeedDialog = false
                    },
                    onDismiss = { showSpeedDialog = false }
                )
            }
            
            if (showVolumeDialog) {
                VolumeControlDialog(
                    currentVolume = state.volume,
                    onVolumeChanged = onVolumeChange,
                    onDismiss = { showVolumeDialog = false }
                )
            }
        }
    }
}

@Composable
private fun VideoPlayerError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Playback Error",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun SpeedSelectionDialog(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Playback Speed") },
        text = {
            Column {
                speeds.forEach { speed ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSpeedSelected(speed) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSpeed == speed,
                            onClick = { onSpeedSelected(speed) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${speed}x")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun VolumeControlDialog(
    currentVolume: Float,
    onVolumeChanged: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Volume Control") },
        text = {
            Column {
                Text("Volume: ${(currentVolume * 100).toInt()}%")
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = currentVolume,
                    onValueChange = onVolumeChanged,
                    valueRange = 0f..1f
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

private fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes % 60, seconds % 60)
    } else {
        "%d:%02d".format(minutes, seconds % 60)
    }
}