package com.heartlessveteran.myriad.feature.browser.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.heartlessveteran.myriad.core.domain.manager.SearchResult
import com.heartlessveteran.myriad.feature.browser.viewmodel.GlobalSearchViewModel
import com.heartlessveteran.myriad.feature.browser.viewmodel.SearchTab

/**
 * Screen for global search across all manga sources
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    viewModel: GlobalSearchViewModel,
    onMangaClick: (SearchResult) -> Unit,
    onNavigateToPlugins: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Show error as snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // In a real app, this would show a snackbar
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse") },
                actions = {
                    IconButton(onClick = onNavigateToPlugins) {
                        Icon(Icons.Default.Settings, contentDescription = "Manage Plugins")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = uiState.query,
                onQueryChange = viewModel::updateQuery,
                onSearch = {
                    keyboardController?.hide()
                    viewModel.search()
                },
                isSearching = uiState.isSearching,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Tab row
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal
            ) {
                Tab(
                    selected = uiState.selectedTab == SearchTab.SEARCH,
                    onClick = { viewModel.selectTab(SearchTab.SEARCH) },
                    text = { Text("Search") },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == SearchTab.LATEST,
                    onClick = { viewModel.selectTab(SearchTab.LATEST) },
                    text = { Text("Latest") },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == SearchTab.POPULAR,
                    onClick = { viewModel.selectTab(SearchTab.POPULAR) },
                    text = { Text("Popular") },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) }
                )
            }

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    uiState.isSearching && uiState.searchResults.results.isEmpty() -> {
                        // Loading state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = when (uiState.selectedTab) {
                                        SearchTab.SEARCH -> "Searching..."
                                        SearchTab.LATEST -> "Loading latest manga..."
                                        SearchTab.POPULAR -> "Loading popular manga..."
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    uiState.searchResults.results.isEmpty() && !uiState.isSearching -> {
                        // Empty state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    when (uiState.selectedTab) {
                                        SearchTab.SEARCH -> Icons.Default.Search
                                        SearchTab.LATEST -> Icons.Default.Refresh
                                        SearchTab.POPULAR -> Icons.Default.Star
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = when (uiState.selectedTab) {
                                        SearchTab.SEARCH -> if (uiState.query.isBlank()) "Enter a search query" else "No results found"
                                        SearchTab.LATEST -> "No latest manga available"
                                        SearchTab.POPULAR -> "No popular manga available"
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (uiState.selectedTab == SearchTab.SEARCH && uiState.query.isNotBlank()) {
                                    Text(
                                        text = "Try a different search term",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        // Results
                        SearchResultsList(
                            groupedResults = uiState.searchResults,
                            onMangaClick = onMangaClick,
                            isLoading = uiState.isSearching
                        )
                    }
                }
            }
        }
    }
}

/**
 * Search bar component
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search manga...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }
        ),
        singleLine = true
    )
}

/**
 * List of search results grouped by source
 */
@Composable
private fun SearchResultsList(
    groupedResults: com.heartlessveteran.myriad.core.domain.manager.GroupedSearchResults,
    onMangaClick: (SearchResult) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedResults.results.forEach { (sourceId, results) ->
            if (results.isNotEmpty()) {
                item(key = "header_$sourceId") {
                    SourceHeader(
                        sourceName = results.first().sourceName,
                        resultCount = results.size
                    )
                }
                
                items(
                    items = results,
                    key = { result -> "${result.sourceId}_${result.manga.id}" }
                ) { result ->
                    MangaCard(
                        searchResult = result,
                        onClick = { onMangaClick(result) }
                    )
                }
            }
        }
        
        if (isLoading && groupedResults.results.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/**
 * Header for each source section
 */
@Composable
private fun SourceHeader(
    sourceName: String,
    resultCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = sourceName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$resultCount result${if (resultCount != 1) "s" else ""}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

/**
 * Card component for displaying manga search results
 */
@Composable
private fun MangaCard(
    searchResult: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val manga = searchResult.manga
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Cover image
            AsyncImage(
                model = manga.coverImageUrl,
                contentDescription = "Cover for ${manga.title}",
                modifier = Modifier
                    .size(80.dp, 120.dp),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Manga details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = manga.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (manga.author.isNotBlank()) {
                        Text(
                            text = manga.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    if (manga.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = manga.description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Status and rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = manga.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (manga.rating > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "%.1f".format(manga.rating),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}