package com.heartlessveteran.myriad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.ui.components.FileImportDialog
import com.heartlessveteran.myriad.ui.components.ImportProgressDialog
import com.heartlessveteran.myriad.ui.viewmodel.BrowseViewModel
import com.heartlessveteran.myriad.ui.viewmodel.FileImportViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel,
    onMangaClick: (manga: Manga) -> Unit = {},
    onImportClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    // File import functionality
    var showImportDialog by remember { mutableStateOf(false) }
    var showProgressDialog by remember { mutableStateOf(false) }
    val fileImportViewModel = remember { FileImportViewModel(context) }
    val importStatus by fileImportViewModel.importStatus.collectAsState()
    
    // Handle import status changes
    LaunchedEffect(importStatus) {
        if (importStatus.isLoading) {
            showImportDialog = false
            showProgressDialog = true
        } else if (importStatus.isComplete) {
            // Keep progress dialog open until user dismisses
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState.isSearching) {
                        Text("Search Results: \"${uiState.searchQuery}\"")
                    } else {
                        Text("Browse MangaDex")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showImportDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Import local manga",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                placeholder = { Text("Search manga...") },
                leadingIcon = {
                    IconButton(onClick = {
                        viewModel.searchManga(searchQuery)
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.loadLatestManga()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
            )

            // Content
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator()
                    }
                    uiState.error != null -> {
                        uiState.error?.let { errorMsg ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Text(
                                    text = errorMsg,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(16.dp),
                                )
                                Button(onClick = { viewModel.retry() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    uiState.manga.isEmpty() -> {
                        Text(
                            text = if (uiState.isSearching) "No results found" else "No manga available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    else -> {
                        MangaGrid(
                            mangaList = uiState.manga,
                            onMangaClick = onMangaClick,
                        )
                    }
                }
            }
        }
    }

    // File import dialogs
    FileImportDialog(
        isVisible = showImportDialog,
        onDismiss = { showImportDialog = false },
        onFileSelected = { uri ->
            fileImportViewModel.importFile(uri)
        },
        onDirectorySelected = { uri ->
            fileImportViewModel.importDirectory(uri)
        }
    )

    ImportProgressDialog(
        isVisible = showProgressDialog,
        onDismiss = { 
            showProgressDialog = false
            fileImportViewModel.clearImportStatus()
        },
        importStatus = importStatus
    )
}

@Composable
private fun MangaGrid(
    mangaList: List<Manga>,
    onMangaClick: (manga: Manga) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(mangaList, key = { it.id }) { manga ->
            MangaGridItem(manga = manga, onClick = { onMangaClick(manga) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MangaGridItem(
    manga: Manga,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Card(onClick = onClick) {
        Column {
            Box {
                AsyncImage(
                    model = manga.coverImageUrl,
                    contentDescription = manga.title,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.7f)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                
                // Download button overlay for online manga
                if (!manga.isLocal) {
                    FloatingActionButton(
                        onClick = {
                            // Demo: Start download using the download service
                            // In a real app, this would be handled by a ViewModel
                            val downloadService = com.heartlessveteran.myriad.di.AppDiContainer.getDownloadService(context)
                            coroutineScope.launch {
                                downloadService.enqueueMangaDownload(manga, null)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(36.dp),
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Download",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Text(
                text = manga.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}
