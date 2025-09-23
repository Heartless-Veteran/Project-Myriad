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
import androidx.compose.material3.Button
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
import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.ui.viewmodel.LibraryEvent
import com.heartlessveteran.myriad.ui.viewmodel.LibraryUiEvent
import com.heartlessveteran.myriad.ui.viewmodel.LibraryViewModel

/**
 * Library screen that displays the user's manga collection.
 * Follows Compose guidelines with stateless composables and proper state management.
 * Uses PascalCase naming as per architecture requirements.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel? = null,
) {
    // For now, we'll handle the case where no viewModel is provided
    // In a real implementation, this would be created with proper DI
    if (viewModel == null) {
        EmptyLibraryContent()
        return
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle one-time UI events
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is LibraryUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is LibraryUiEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                LibraryUiEvent.NavigateToReader -> {
                    // Handle navigation
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Library") },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
        ) {
            // Search field
            SearchField(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onEvent(LibraryEvent.SearchManga(it)) },
            )

            // Content
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                uiState.manga.isEmpty() -> {
                    EmptyLibraryContent()
                }
                else -> {
                    MangaList(
                        manga = uiState.manga,
                        onMangaClick = { mangaId ->
                            viewModel.getMangaDetails(mangaId)
                        },
                    )
                }
            }
        }
    }
}

/**
 * Composable for the search field.
 * Stateless component that delegates state management to parent.
 */
@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search manga...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
            )
        },
        singleLine = true,
    )
}

/**
 * Composable for displaying the manga list.
 * Uses LazyColumn for performance with large lists.
 */
@Composable
private fun MangaList(
    manga: List<Manga>,
    onMangaClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(manga) { manga ->
            MangaItem(
                manga = manga,
                onClick = { onMangaClick(manga.id) },
            )
        }
    }
}

/**
 * Composable for individual manga item.
 * Stateless component focused on single responsibility.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MangaItem(
    manga: Manga,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = manga.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (manga.author.isNotBlank()) {
                Text(
                    text = "By ${manga.author}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (manga.totalChapters > 0) {
                Text(
                    text = "${manga.readChapters}/${manga.totalChapters} chapters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Composable for loading state.
 * Centered loading indicator following Material Design guidelines.
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
 * Composable for empty library state.
 * Provides guidance to users when no manga is available.
 */
@Composable
private fun EmptyLibraryContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Your library is empty",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Add some manga to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            
            // Add demo reader access
            Button(
                onClick = { /* Navigate to Reader demo */ },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Demo Reader")
            }
        }
    }
}
