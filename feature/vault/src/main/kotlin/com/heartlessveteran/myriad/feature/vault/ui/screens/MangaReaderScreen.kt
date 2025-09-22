package com.heartlessveteran.myriad.feature.vault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.heartlessveteran.myriad.feature.vault.viewmodel.MangaReaderViewModel
import com.heartlessveteran.myriad.feature.vault.viewmodel.MangaReaderUiState
import com.heartlessveteran.myriad.feature.vault.viewmodel.ReadingMode
import com.heartlessveteran.myriad.feature.vault.viewmodel.ReadingDirection
import kotlinx.coroutines.launch

/**
 * Enhanced manga reader screen with multiple reading modes and progress persistence.
 * Supports gesture controls, zoom, pan, and various reading layouts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaReaderScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: MangaReaderViewModel? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { 
        mutableStateOf(MangaReaderUiState()) 
    }
    val readingProgress by viewModel?.readingProgress?.collectAsState() ?: remember { 
        mutableStateOf(null) 
    }
    
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = uiState.currentPage,
        pageCount = { uiState.pages.size }
    )
    
    // Sample pages for demo if none provided
    val displayPages = remember(uiState.pages) {
        if (uiState.pages.isNotEmpty()) uiState.pages else listOf(
            "Sample Page 1",
            "Sample Page 2", 
            "Sample Page 3",
            "Sample Page 4",
            "Sample Page 5"
        )
    }

    // Sync pager with view model
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage) {
            viewModel?.updateCurrentPage(pagerState.currentPage)
        }
    }

    // Sync view model with pager
    LaunchedEffect(uiState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage && uiState.currentPage in 0 until displayPages.size) {
            pagerState.animateScrollToPage(uiState.currentPage)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { viewModel?.toggleUIVisibility() }
    ) {
        // Main reading content
        when (uiState.readingMode) {
            ReadingMode.SINGLE_PAGE -> {
                SinglePageReader(
                    pages = displayPages,
                    pagerState = pagerState,
                    uiState = uiState,
                    onZoomChange = { viewModel?.setZoomLevel(it) }
                )
            }
            ReadingMode.DOUBLE_PAGE -> {
                DoublePageReader(
                    pages = displayPages,
                    currentPage = uiState.currentPage,
                    onPageChange = { viewModel?.updateCurrentPage(it) }
                )
            }
            ReadingMode.CONTINUOUS_VERTICAL -> {
                ContinuousVerticalReader(
                    pages = displayPages,
                    currentPage = uiState.currentPage,
                    onPageChange = { viewModel?.updateCurrentPage(it) }
                )
            }
            ReadingMode.WEBTOON -> {
                WebtoonReader(
                    pages = displayPages,
                    currentPage = uiState.currentPage,
                    onPageChange = { viewModel?.updateCurrentPage(it) }
                )
            }
        }

        // UI Overlay
        if (uiState.isUIVisible) {
            ReaderUIOverlay(
                uiState = uiState,
                onNavigateBack = onNavigateBack,
                onSettingsClick = { showSettingsDialog = true },
                onPreviousPage = { viewModel?.previousPage() },
                onNextPage = { viewModel?.nextPage() },
                readingProgress = readingProgress
            )
        }

        // Settings Dialog
        if (showSettingsDialog) {
            ReaderSettingsDialog(
                currentMode = uiState.readingMode,
                currentDirection = uiState.readingDirection,
                onModeChange = { viewModel?.changeReadingMode(it) },
                onDirectionChange = { viewModel?.changeReadingDirection(it) },
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}

@Composable
private fun SinglePageReader(
    pages: List<String>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    uiState: MangaReaderUiState,
    onZoomChange: (Float) -> Unit
) {
    var scale by remember { mutableFloatStateOf(uiState.zoomLevel) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 3f)
        offsetX += panChange.x
        offsetY += panChange.y
        onZoomChange(scale)
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(transformableState)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentAlignment = Alignment.Center
        ) {
            PageContent(pages[page], page + 1, pages.size)
        }
    }
}

@Composable
private fun DoublePageReader(
    pages: List<String>,
    currentPage: Int,
    onPageChange: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items((pages.size + 1) / 2) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val leftPageIndex = index * 2
                val rightPageIndex = leftPageIndex + 1
                
                if (leftPageIndex < pages.size) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.7f)
                            .clickable { onPageChange(leftPageIndex) }
                    ) {
                        PageContent(pages[leftPageIndex], leftPageIndex + 1, pages.size)
                    }
                }
                
                if (rightPageIndex < pages.size) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.7f)
                            .clickable { onPageChange(rightPageIndex) }
                    ) {
                        PageContent(pages[rightPageIndex], rightPageIndex + 1, pages.size)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ContinuousVerticalReader(
    pages: List<String>,
    currentPage: Int,
    onPageChange: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(pages.size) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onPageChange(index) }
            ) {
                PageContent(pages[index], index + 1, pages.size)
            }
        }
    }
}

@Composable
private fun WebtoonReader(
    pages: List<String>,
    currentPage: Int,
    onPageChange: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(pages.size) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPageChange(index) }
            ) {
                // Webtoon style - pages flow continuously
                PageContent(pages[index], index + 1, pages.size, isWebtoon = true)
            }
        }
    }
}

@Composable
private fun PageContent(
    content: String,
    pageNumber: Int,
    totalPages: Int,
    isWebtoon: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isWebtoon) Modifier.wrapContentHeight()
                else Modifier.aspectRatio(0.7f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Page $pageNumber of $totalPages",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReaderUIOverlay(
    uiState: MangaReaderUiState,
    onNavigateBack: () -> Unit,
    onSettingsClick: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    readingProgress: com.heartlessveteran.myriad.feature.vault.domain.usecase.ReadingProgress?
) {
    // Top bar
    TopAppBar(
        title = {
            Column {
                Text(
                    text = uiState.mangaTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = uiState.chapterTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Reading Settings",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        )
    )

    // Bottom controls
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPreviousPage,
                enabled = uiState.currentPage > 0
            ) {
                Text("Previous")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${uiState.currentPage + 1} / ${uiState.totalPages}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                LinearProgressIndicator(
                    progress = if (uiState.totalPages > 0) {
                        (uiState.currentPage + 1).toFloat() / uiState.totalPages
                    } else 0f,
                    modifier = Modifier.width(120.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                readingProgress?.let {
                    Text(
                        text = "${(it.progressPercentage * 100).toInt()}% complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Button(
                onClick = onNextPage,
                enabled = uiState.currentPage < uiState.totalPages - 1
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
private fun ReaderSettingsDialog(
    currentMode: ReadingMode,
    currentDirection: ReadingDirection,
    onModeChange: (ReadingMode) -> Unit,
    onDirectionChange: (ReadingDirection) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reading Settings") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Reading Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                ReadingMode.values().forEach { mode ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModeChange(mode) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = currentMode == mode,
                            onClick = { onModeChange(mode) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (mode) {
                                ReadingMode.SINGLE_PAGE -> "Single Page"
                                ReadingMode.DOUBLE_PAGE -> "Double Page"
                                ReadingMode.CONTINUOUS_VERTICAL -> "Continuous Vertical"
                                ReadingMode.WEBTOON -> "Webtoon"
                            }
                        )
                    }
                }
                
                Divider()
                
                Text(
                    text = "Reading Direction",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                ReadingDirection.values().forEach { direction ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDirectionChange(direction) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = currentDirection == direction,
                            onClick = { onDirectionChange(direction) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (direction) {
                                ReadingDirection.LEFT_TO_RIGHT -> "Left to Right"
                                ReadingDirection.RIGHT_TO_LEFT -> "Right to Left"
                                ReadingDirection.VERTICAL -> "Vertical"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun MangaReaderScreenPreview() {
    MaterialTheme {
        MangaReaderScreen()
    }
}