package com.heartlessveteran.myriad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.ui.theme.MangaAccent
import com.heartlessveteran.myriad.ui.viewmodel.MangaFilter
import com.heartlessveteran.myriad.ui.viewmodel.MangaLibraryViewModel

/**
 * Manga Library Screen with Material 3 design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaLibraryScreen(
    onMangaClick: (String) -> Unit,
    viewModel: MangaLibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = "Manga Library",
                    color = MangaAccent,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { showFilterMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter"
                    )
                }
                IconButton(onClick = { /* TODO: Add new manga */ }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Manga"
                    )
                }
            }
        )
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                viewModel.searchManga(it)
            },
            label = { Text("Search manga...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true
        )
        
        // Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MangaFilter.values()) { filter ->
                FilterChip(
                    onClick = { viewModel.applyFilter(filter) },
                    label = { Text(getFilterDisplayName(filter)) },
                    selected = uiState.selectedFilter == filter
                )
            }
        }
        
        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Loading manga library...")
                        }
                    }
                }
                
                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "An unexpected error occurred. Please try again.",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { viewModel.refresh() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                uiState.filteredMangaList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = if (uiState.searchQuery.isNotBlank()) 
                                    "No manga found for \"${uiState.searchQuery}\"" 
                                else "Your manga library is empty",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = if (uiState.searchQuery.isNotBlank()) 
                                    "Try a different search term" 
                                else "Add manga to get started",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.filteredMangaList) { manga ->
                            MangaListItem(
                                manga = manga,
                                onClick = { onMangaClick(manga.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(manga.id) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Error snackbar
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar
            viewModel.clearError()
        }
    }
}

@Composable
private fun MangaListItem(
    manga: Manga,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cover Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(manga.coverImageUrl ?: manga.localCoverPath)
                    .crossfade(true)
                    .build(),
                contentDescription = "Cover for ${manga.title}",
                modifier = Modifier
                    .width(60.dp)
                    .height(90.dp),
                contentScale = ContentScale.Crop
            )
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = manga.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (manga.author.isNotBlank()) {
                    Text(
                        text = "by ${manga.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "${manga.readChapters}/${if (manga.totalChapters > 0) manga.totalChapters else "?"} chapters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Progress bar
                if (manga.totalChapters > 0) {
                    LinearProgressIndicator(
                        progress = manga.readChapters.toFloat() / manga.totalChapters,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Favorite button
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (manga.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (manga.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (manga.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getFilterDisplayName(filter: MangaFilter): String = when (filter) {
    MangaFilter.ALL -> "All"
    MangaFilter.FAVORITES -> "Favorites"
    MangaFilter.READING -> "Reading"
    MangaFilter.COMPLETED -> "Completed"
    MangaFilter.ON_HOLD -> "On Hold"
    MangaFilter.DROPPED -> "Dropped"
    MangaFilter.UNREAD -> "Unread"
}