package com.heartlessveteran.myriad.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.heartlessveteran.myriad.ui.components.EnhancedVideoPlayerControls
import com.heartlessveteran.myriad.ui.viewmodel.AnimePlayerEvent
import com.heartlessveteran.myriad.ui.viewmodel.AnimePlayerViewModel

/**
 * Anime player screen that displays video content using ExoPlayer.
 * Supports basic playback controls and episode navigation.
 * Follows Compose guidelines with proper lifecycle management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimePlayerScreen(
    animeId: String,
    episodeId: String,
    modifier: Modifier = Modifier,
    viewModel: AnimePlayerViewModel? = null,
    onNavigateBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel?.uiState?.collectAsState() ?: return

    // Create ExoPlayer instance
    val exoPlayer =
        remember {
            ExoPlayer
                .Builder(context)
                .build()
                .apply {
                    // Configure player settings
                    playWhenReady = true
                    repeatMode = Player.REPEAT_MODE_OFF
                }
        }

    // Load the episode when the screen is displayed
    LaunchedEffect(animeId, episodeId) {
        viewModel?.onEvent(AnimePlayerEvent.LoadEpisode(animeId, episodeId))
    }

    // Update player source when episode data changes
    LaunchedEffect(uiState.currentEpisode) {
        uiState.currentEpisode?.let { episode ->
            episode.localPath?.let { path ->
                val mediaItem = MediaItem.fromUri(path)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()

                // Restore playback position if available
                if (episode.watchProgress > 0) {
                    exoPlayer.seekTo(episode.watchProgress)
                }
            }
        }
    }

    // Handle player lifecycle
    DisposableEffect(exoPlayer) {
        onDispose {
            // Save current position before disposing
            val currentPosition = exoPlayer.currentPosition
            viewModel?.onEvent(
                AnimePlayerEvent.UpdateProgress(
                    episodeId,
                    currentPosition,
                ),
            )
            exoPlayer.release()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.currentEpisode?.title ?: "Loading...",
                        color = Color.White,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.7f),
                    ),
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(paddingValues),
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Loading episode...",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            } else if (uiState.errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        color = Color.Red,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            } else {
                // ExoPlayer View
                AndroidView(
                    factory = { context ->
                        PlayerView(context).apply {
                            player = exoPlayer
                            layoutParams =
                                FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                )
                            useController = false // We'll use our custom controls
                            controllerAutoShow = false
                            controllerHideOnTouch = false
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                // Enhanced custom controls overlay
                EnhancedVideoPlayerControls(
                    uiState = uiState,
                    onEvent = { event -> viewModel?.onEvent(event) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
