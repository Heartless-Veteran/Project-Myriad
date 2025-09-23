package com.heartlessveteran.myriad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Enhanced Reader Screen - Phase 2 Implementation
 * Advanced reader with multiple reading modes, gesture controls, and settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedReaderScreen(
    mangaTitle: String = "Sample Manga",
    chapterTitle: String = "Chapter 1",
    currentPage: Int = 1,
    totalPages: Int = 20,
    readingMode: ReadingMode = ReadingMode.SINGLE_PAGE,
    onNavigateBack: () -> Unit = {},
    onPageChange: (Int) -> Unit = {},
    onReadingModeChange: (ReadingMode) -> Unit = {}
) {
    var page by remember { mutableIntStateOf(currentPage) }
    var showSettings by remember { mutableStateOf(false) }
    var brightness by remember { mutableFloatStateOf(1.0f) }
    var isFullscreen by remember { mutableStateOf(false) }
    
    if (isFullscreen) {
        FullscreenReader(
            page = page,
            totalPages = totalPages,
            readingMode = readingMode,
            onPageChange = { 
                page = it
                onPageChange(it)
            },
            onExitFullscreen = { isFullscreen = false },
            onNavigateBack = onNavigateBack
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = mangaTitle,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "$chapterTitle - Page $page of $totalPages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSettings = !showSettings }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                        IconButton(onClick = { isFullscreen = true }) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (!showSettings) {
                    EnhancedReaderControls(
                        currentPage = page,
                        totalPages = totalPages,
                        readingMode = readingMode,
                        onPageChange = { newPage ->
                            page = newPage
                            onPageChange(newPage)
                        },
                        onReadingModeChange = onReadingModeChange
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Progress indicator
                LinearProgressIndicator(
                    progress = { page.toFloat() / totalPages },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (showSettings) {
                    ReaderSettings(
                        brightness = brightness,
                        onBrightnessChange = { brightness = it },
                        readingMode = readingMode,
                        onReadingModeChange = onReadingModeChange,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Page content based on reading mode
                    ReaderContent(
                        page = page,
                        readingMode = readingMode,
                        brightness = brightness,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

enum class ReadingMode {
    SINGLE_PAGE,
    DOUBLE_PAGE,
    CONTINUOUS,
    WEBTOON
}

@Composable
private fun EnhancedReaderControls(
    currentPage: Int,
    totalPages: Int,
    readingMode: ReadingMode,
    onPageChange: (Int) -> Unit,
    onReadingModeChange: (ReadingMode) -> Unit
) {
    Surface(
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Reading mode selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                ReadingMode.values().forEach { mode ->
                    FilterChip(
                        onClick = { onReadingModeChange(mode) },
                        label = { 
                            Text(
                                text = when(mode) {
                                    ReadingMode.SINGLE_PAGE -> "Single"
                                    ReadingMode.DOUBLE_PAGE -> "Double"
                                    ReadingMode.CONTINUOUS -> "Scroll"
                                    ReadingMode.WEBTOON -> "Webtoon"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        selected = readingMode == mode,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            
            // Page navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        if (currentPage > 1) onPageChange(currentPage - 1) 
                    },
                    enabled = currentPage > 1
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous Page"
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currentPage.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    Slider(
                        value = currentPage.toFloat(),
                        onValueChange = { onPageChange(it.toInt()) },
                        valueRange = 1f..totalPages.toFloat(),
                        steps = totalPages - 2,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = totalPages.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                IconButton(
                    onClick = { 
                        if (currentPage < totalPages) onPageChange(currentPage + 1) 
                    },
                    enabled = currentPage < totalPages
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next Page"
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderSettings(
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    readingMode: ReadingMode,
    onReadingModeChange: (ReadingMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Reader Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        // Brightness Control
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Brightness",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.BrightnessHigh, "Brightness")
                    Slider(
                        value = brightness,
                        onValueChange = onBrightnessChange,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${(brightness * 100).toInt()}%")
                }
            }
        }
        
        // Reading Mode
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Reading Mode",
                    style = MaterialTheme.typography.titleMedium
                )
                
                ReadingMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = readingMode == mode,
                            onClick = { onReadingModeChange(mode) }
                        )
                        Text(
                            text = when(mode) {
                                ReadingMode.SINGLE_PAGE -> "Single Page - Traditional page by page"
                                ReadingMode.DOUBLE_PAGE -> "Double Page - Two pages side by side"
                                ReadingMode.CONTINUOUS -> "Continuous Scroll - Smooth scrolling"
                                ReadingMode.WEBTOON -> "Webtoon - Vertical strip format"
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
        
        // Additional Settings
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Advanced Settings",
                    style = MaterialTheme.typography.titleMedium
                )
                
                var keepScreenOn by remember { mutableStateOf(true) }
                var cropBorders by remember { mutableStateOf(false) }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Keep screen on")
                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = { keepScreenOn = it }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Crop page borders")
                    Switch(
                        checked = cropBorders,
                        onCheckedChange = { cropBorders = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderContent(
    page: Int,
    readingMode: ReadingMode,
    brightness: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (readingMode) {
            ReadingMode.SINGLE_PAGE -> SinglePageContent(page, brightness)
            ReadingMode.DOUBLE_PAGE -> DoublePageContent(page, brightness)
            ReadingMode.CONTINUOUS -> ContinuousContent(page, brightness)
            ReadingMode.WEBTOON -> WebtoonContent(page, brightness)
        }
    }
}

@Composable
private fun SinglePageContent(page: Int, brightness: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(0.7f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📖 Page $page\n\nSingle Page Mode\nBrightness: ${(brightness * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DoublePageContent(page: Int, brightness: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(0.95f),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(2) { index ->
            Card(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.7f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📖\nPage ${page + index}\n\nDouble Mode",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ContinuousContent(page: Int, brightness: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(400.dp)
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📖 Page ${page + index}\n\nContinuous Scroll Mode",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun WebtoonContent(page: Int, brightness: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(5) { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📱 Panel ${page + index}\n\nWebtoon Mode\nVertical Strip",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun FullscreenReader(
    page: Int,
    totalPages: Int,
    readingMode: ReadingMode,
    onPageChange: (Int) -> Unit,
    onExitFullscreen: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        ReaderContent(
            page = page,
            readingMode = readingMode,
            brightness = 1.0f,
            modifier = Modifier.fillMaxSize()
        )
        
        // Minimal overlay controls
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            ) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
            
            TextButton(
                onClick = onExitFullscreen,
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            ) {
                Text("Exit Fullscreen")
            }
        }
        
        Text(
            text = "$page / $totalPages",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}