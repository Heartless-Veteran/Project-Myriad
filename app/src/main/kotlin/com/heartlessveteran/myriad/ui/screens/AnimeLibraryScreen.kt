package com.heartlessveteran.myriad.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.heartlessveteran.myriad.core.domain.entities.Anime
import com.heartlessveteran.myriad.ui.viewmodel.AnimeLibraryEvent
import com.heartlessveteran.myriad.ui.viewmodel.AnimeLibraryUiEvent
import com.heartlessveteran.myriad.ui.viewmodel.AnimeLibraryViewModel

/**
 * Anime library screen that displays the user's anime collection.
 * Follows Compose guidelines with stateless composables and proper state management.
 * Uses PascalCase naming as per architecture requirements.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeLibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: AnimeLibraryViewModel? = null,
) {
    // For now, we'll handle the case where no viewModel is provided
    // In a real implementation, this would be created with proper DI
    if (viewModel == null) {
        EmptyAnimeLibraryContent()
        return
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle UI events
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AnimeLibraryUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        withDismissAction = true,
                    )
                }
                is AnimeLibraryUiEvent.NavigateToAnimeDetails -> {
                    // TODO: Navigate to anime details screen
                }
                is AnimeLibraryUiEvent.NavigateToPlayer -> {
                    // TODO: Navigate to video player
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Anime Library") },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingContent(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                )
            }
            uiState.anime.isEmpty() -> {
                EmptyAnimeLibraryContent(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                )
            }
            else -> {
                AnimeLibraryContent(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    anime = uiState.anime,
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { query ->
                        viewModel.onEvent(AnimeLibraryEvent.SearchQueryChanged(query))
                    },
                    onAnimeClick = { anime ->
                        viewModel.onEvent(AnimeLibraryEvent.AnimeClicked(anime))
                    },
                )
            }
        }
    }
}

/**
 * Composable for the main anime library content.
 * Displays search bar and list of anime.
 */
@Composable
private fun AnimeLibraryContent(
    modifier: Modifier = Modifier,
    anime: List<Anime>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAnimeClick: (Anime) -> Unit,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search anime...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                )
            },
            singleLine = true,
        )

        // Anime list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(anime) { animeItem ->
                AnimeListItem(
                    anime = animeItem,
                    onClick = { onAnimeClick(animeItem) },
                )
            }
        }
    }
}

/**
 * Composable for individual anime list item.
 * Displays anime information in a card format.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimeListItem(
    anime: Anime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = anime.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (anime.description.isNotBlank()) {
                Text(
                    text = anime.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Episode progress
            val progressText =
                if (anime.totalEpisodes > 0) {
                    "${anime.watchedEpisodes}/${anime.totalEpisodes} episodes"
                } else {
                    "${anime.watchedEpisodes} episodes watched"
                }

            Text(
                text = progressText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )

            // Genres
            if (anime.genres.isNotEmpty()) {
                Text(
                    text = anime.genres.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Composable for loading state.
 * Shows a circular progress indicator while loading.
 */
@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Composable for empty anime library state.
 * Provides guidance to users when no anime is available.
 */
@Composable
private fun EmptyAnimeLibraryContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Your anime library is empty",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Add some anime to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
