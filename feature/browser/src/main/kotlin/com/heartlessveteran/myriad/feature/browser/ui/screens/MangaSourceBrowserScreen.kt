package com.heartlessveteran.myriad.feature.browser.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.entities.MangaStatus

/**
 * Screen for browsing online manga sources like MangaDx.
 * Provides search, popular, and latest manga discovery.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaSourceBrowserScreen(
    onNavigateBack: () -> Unit = {},
    onMangaClick: (Manga) -> Unit = {},
    onDownloadManga: (Manga) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Sample data for preview - in production this would come from a ViewModel
    val sampleManga = remember {
        listOf(
            Manga(
                id = "1",
                title = "One Piece",
                description = "Epic pirate adventure spanning over 1000 chapters",
                author = "Eiichiro Oda",
                status = MangaStatus.ONGOING,
                genres = listOf("Adventure", "Comedy", "Drama"),
                coverImageUrl = "https://example.com/onepiece.jpg",
                source = "mangadx",
                isLocal = false
            ),
            Manga(
                id = "2",
                title = "Attack on Titan",
                description = "Humanity's fight against giant titans",
                author = "Hajime Isayama",
                status = MangaStatus.COMPLETED,
                genres = listOf("Action", "Drama", "Fantasy"),
                coverImageUrl = "https://example.com/aot.jpg",
                source = "mangadx",
                isLocal = false
            ),
            Manga(
                id = "3",
                title = "Demon Slayer",
                description = "A boy becomes a demon slayer to save his sister",
                author = "Koyoharu Gotouge",
                status = MangaStatus.COMPLETED,
                genres = listOf("Action", "Supernatural"),
                coverImageUrl = "https://example.com/demonslayer.jpg",
                source = "mangadx",
                isLocal = false
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse MangaDx") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Search bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search manga...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            isSearching = true
                            keyboardController?.hide()
                            // TODO: Trigger search in ViewModel
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            // Tab navigation
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Popular") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Latest") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Search Results") },
                    enabled = searchQuery.isNotEmpty()
                )
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> {
                    MangaGrid(
                        manga = sampleManga,
                        onMangaClick = onMangaClick,
                        onDownloadClick = onDownloadManga,
                        modifier = Modifier.weight(1f)
                    )
                }
                1 -> {
                    MangaGrid(
                        manga = sampleManga.reversed(), // Different order for latest
                        onMangaClick = onMangaClick,
                        onDownloadClick = onDownloadManga,
                        modifier = Modifier.weight(1f)
                    )
                }
                2 -> {
                    if (searchQuery.isNotEmpty()) {
                        if (isSearching) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CircularProgressIndicator()
                                    Text("Searching MangaDx...")
                                }
                            }
                        } else {
                            MangaGrid(
                                manga = sampleManga.filter { 
                                    it.title.contains(searchQuery, ignoreCase = true) 
                                },
                                onMangaClick = onMangaClick,
                                onDownloadClick = onDownloadManga,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Enter search query above",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MangaGrid(
    manga: List<Manga>,
    onMangaClick: (Manga) -> Unit,
    onDownloadClick: (Manga) -> Unit,
    modifier: Modifier = Modifier
) {
    if (manga.isNotEmpty()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
        ) {
            items(manga) { mangaItem ->
                MangaGridItem(
                    manga = mangaItem,
                    onClick = { onMangaClick(mangaItem) },
                    onDownloadClick = { onDownloadClick(mangaItem) }
                )
            }
        }
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "No manga found",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MangaGridItem(
    manga: Manga,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Cover image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f)
            ) {
                if (manga.coverImageUrl != null) {
                    AsyncImage(
                        model = manga.coverImageUrl,
                        contentDescription = manga.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Download button overlay
                IconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Download",
                            modifier = Modifier.padding(4.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Manga info
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = manga.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = manga.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Status and genre chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { },
                        label = { 
                            Text(
                                text = manga.status.name,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (manga.status) {
                                MangaStatus.ONGOING -> MaterialTheme.colorScheme.primaryContainer
                                MangaStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MangaSourceBrowserScreenPreview() {
    MaterialTheme {
        MangaSourceBrowserScreen()
    }
}