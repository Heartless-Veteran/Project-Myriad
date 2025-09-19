package com.heartlessveteran.myriad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.heartlessveteran.myriad.ui.theme.MyriadTheme
import kotlin.math.max
import kotlin.math.min

/**
 * Enhanced manga reading screen with advanced features.
 *
 * This screen provides:
 * - Multiple reading modes (LTR, RTL, vertical, webtoon, double-page)
 * - Advanced zoom and scaling with gesture support
 * - Customizable background colors and themes
 * - Enhanced navigation controls
 * - Reading progress tracking
 * - Bookmark and chapter management
 */

/**
 * Reading modes supported by the reader
 */
enum class ReadingMode {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    VERTICAL,
    WEBTOON,
    DOUBLE_PAGE,
}

/**
 * Reader configuration options
 */
data class ReaderConfiguration(
    val readingMode: ReadingMode = ReadingMode.LEFT_TO_RIGHT,
    val backgroundColor: ReaderBackgroundColor = ReaderBackgroundColor.BLACK,
    val zoomMode: ZoomMode = ZoomMode.FIT_WIDTH,
    val enableGestures: Boolean = true,
    val showPageNumbers: Boolean = true,
    val keepScreenOn: Boolean = true,
    val enableDoubleTapZoom: Boolean = true,
    val volumeKeyNavigation: Boolean = true,
)

/**
 * Background color options for the reader
 */
enum class ReaderBackgroundColor(
    val displayName: String,
    val color: Color,
) {
    BLACK("Black", Color.Black),
    WHITE("White", Color.White),
    GRAY("Gray", Color(0xFF424242)),
    SEPIA("Sepia", Color(0xFFF4F1E8)),
}

/**
 * Zoom mode options
 */
enum class ZoomMode {
    FIT_WIDTH,
    FIT_HEIGHT,
    FIT_SCREEN,
    ORIGINAL_SIZE,
    CUSTOM,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedReadingScreen(
    mangaTitle: String,
    chapterTitle: String,
    pages: List<String>,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    onBackPress: () -> Unit,
    configuration: ReaderConfiguration = ReaderConfiguration(),
    onConfigurationChanged: (ReaderConfiguration) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var isMenuVisible by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var readerConfig by remember { mutableStateOf(configuration) }

    // Zoom and pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    LaunchedEffect(currentPage, readerConfig.zoomMode) {
        // Reset zoom when page changes or zoom mode changes
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(readerConfig.backgroundColor.color),
    ) {
        // Main reading content
        when (readerConfig.readingMode) {
            ReadingMode.LEFT_TO_RIGHT, ReadingMode.RIGHT_TO_LEFT -> {
                HorizontalReader(
                    pages = pages,
                    currentPage = currentPage,
                    onPageChanged = onPageChanged,
                    configuration = readerConfig,
                    scale = scale,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    onScaleChange = { scale = it },
                    onOffsetChange = { x, y ->
                        offsetX = x
                        offsetY = y
                    },
                    onMenuToggle = { isMenuVisible = !isMenuVisible },
                )
            }
            ReadingMode.VERTICAL, ReadingMode.WEBTOON -> {
                VerticalReader(
                    pages = pages,
                    currentPage = currentPage,
                    onPageChanged = onPageChanged,
                    configuration = readerConfig,
                    onMenuToggle = { isMenuVisible = !isMenuVisible },
                )
            }
            ReadingMode.DOUBLE_PAGE -> {
                DoublePageReader(
                    pages = pages,
                    currentPage = currentPage,
                    onPageChanged = onPageChanged,
                    configuration = readerConfig,
                    scale = scale,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    onScaleChange = { scale = it },
                    onOffsetChange = { x, y ->
                        offsetX = x
                        offsetY = y
                    },
                    onMenuToggle = { isMenuVisible = !isMenuVisible },
                )
            }
        }

        // Menu overlay
        if (isMenuVisible) {
            ReaderMenuOverlay(
                mangaTitle = mangaTitle,
                chapterTitle = chapterTitle,
                currentPage = currentPage,
                totalPages = pages.size,
                onBackPress = onBackPress,
                onSettingsClick = { showSettings = true },
                onPageSeek = onPageChanged,
                configuration = readerConfig,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

        // Settings dialog
        if (showSettings) {
            ReaderSettingsDialog(
                configuration = readerConfig,
                onConfigurationChanged = { newConfig ->
                    readerConfig = newConfig
                    onConfigurationChanged(newConfig)
                },
                onDismiss = { showSettings = false },
            )
        }
    }
}

@Composable
private fun HorizontalReader(
    pages: List<String>,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    configuration: ReaderConfiguration,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Float, Float) -> Unit,
    onMenuToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (pages.isNotEmpty() && currentPage in 0 until pages.size) {
            AsyncImage(
                model = pages[currentPage],
                contentDescription = "Page ${currentPage + 1}",
                contentScale =
                    when (configuration.zoomMode) {
                        ZoomMode.FIT_WIDTH -> ContentScale.FillWidth
                        ZoomMode.FIT_HEIGHT -> ContentScale.FillHeight
                        ZoomMode.FIT_SCREEN -> ContentScale.Fit
                        ZoomMode.ORIGINAL_SIZE -> ContentScale.None
                        ZoomMode.CUSTOM -> ContentScale.None
                    },
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY,
                        ).pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val screenWidth = size.width
                                    when {
                                        offset.x < screenWidth * 0.3f -> {
                                            // Left tap
                                            val newPage =
                                                if (configuration.readingMode == ReadingMode.RIGHT_TO_LEFT) {
                                                    min(currentPage + 1, pages.size - 1)
                                                } else {
                                                    max(currentPage - 1, 0)
                                                }
                                            onPageChanged(newPage)
                                        }
                                        offset.x > screenWidth * 0.7f -> {
                                            // Right tap
                                            val newPage =
                                                if (configuration.readingMode == ReadingMode.RIGHT_TO_LEFT) {
                                                    max(currentPage - 1, 0)
                                                } else {
                                                    min(currentPage + 1, pages.size - 1)
                                                }
                                            onPageChanged(newPage)
                                        }
                                        else -> {
                                            // Center tap - toggle menu
                                            onMenuToggle()
                                        }
                                    }
                                },
                                onDoubleTap = { _ ->
                                    if (configuration.enableDoubleTapZoom) {
                                        val newScale = if (scale > 1f) 1f else 2f
                                        onScaleChange(newScale)
                                        if (newScale == 1f) {
                                            onOffsetChange(0f, 0f)
                                        }
                                    }
                                },
                            )
                        }.pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                if (configuration.enableGestures) {
                                    val newScale = (scale * zoom).coerceIn(0.5f, 5f)
                                    onScaleChange(newScale)

                                    val newOffsetX = offsetX + pan.x
                                    val newOffsetY = offsetY + pan.y
                                    onOffsetChange(newOffsetX, newOffsetY)
                                }
                            }
                        },
            )
        }
    }
}

@Composable
private fun VerticalReader(
    pages: List<String>,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    configuration: ReaderConfiguration,
    onMenuToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentPage) {
        listState.animateScrollToItem(currentPage)
    }

    LazyColumn(
        state = listState,
        modifier =
            modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            val screenWidth = size.width
                            if (offset.x > screenWidth * 0.3f && offset.x < screenWidth * 0.7f) {
                                onMenuToggle()
                            }
                        },
                    )
                },
        verticalArrangement =
            if (configuration.readingMode == ReadingMode.WEBTOON) {
                Arrangement.Top
            } else {
                Arrangement.spacedBy(8.dp)
            },
    ) {
        items(pages.size) { index ->
            AsyncImage(
                model = pages[index],
                contentDescription = "Page ${index + 1}",
                contentScale = ContentScale.FillWidth,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .then(
                            if (configuration.readingMode == ReadingMode.VERTICAL) {
                                Modifier.padding(horizontal = 8.dp)
                            } else {
                                Modifier
                            },
                        ),
            )
        }
    }
}

@Composable
private fun DoublePageReader(
    pages: List<String>,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    configuration: ReaderConfiguration,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Float, Float) -> Unit,
    onMenuToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            val screenWidth = size.width
                            when {
                                offset.x < screenWidth * 0.3f -> {
                                    onPageChanged(max(currentPage - 2, 0))
                                }
                                offset.x > screenWidth * 0.7f -> {
                                    onPageChanged(min(currentPage + 2, pages.size - 1))
                                }
                                else -> {
                                    onMenuToggle()
                                }
                            }
                        },
                    )
                },
    ) {
        // Left page
        if (currentPage < pages.size) {
            AsyncImage(
                model = pages[currentPage],
                contentDescription = "Page ${currentPage + 1}",
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY,
                        ),
            )
        }

        // Right page
        if (currentPage + 1 < pages.size) {
            AsyncImage(
                model = pages[currentPage + 1],
                contentDescription = "Page ${currentPage + 2}",
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY,
                        ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderMenuOverlay(
    mangaTitle: String,
    chapterTitle: String,
    currentPage: Int,
    totalPages: Int,
    onBackPress: () -> Unit,
    onSettingsClick: () -> Unit,
    onPageSeek: (Int) -> Unit,
    configuration: ReaderConfiguration,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Top bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = mangaTitle,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                    Text(
                        text = chapterTitle,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackPress) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* TODO: Bookmark */ }) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark")
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                ),
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bottom controls
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                // Page slider
                if (configuration.showPageNumbers && totalPages > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${currentPage + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(40.dp),
                        )

                        Slider(
                            value = currentPage.toFloat(),
                            onValueChange = { onPageSeek(it.toInt()) },
                            valueRange = 0f..(totalPages - 1).toFloat(),
                            steps = if (totalPages > 2) totalPages - 2 else 0,
                            modifier = Modifier.weight(1f),
                        )

                        Text(
                            text = "$totalPages",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(40.dp),
                        )
                    }
                }

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    IconButton(
                        onClick = { onPageSeek(max(currentPage - 1, 0)) },
                        enabled = currentPage > 0,
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                    }

                    IconButton(
                        onClick = { onPageSeek(min(currentPage + 1, totalPages - 1)) },
                        enabled = currentPage < totalPages - 1,
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderSettingsDialog(
    configuration: ReaderConfiguration,
    onConfigurationChanged: (ReaderConfiguration) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reader Settings") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    // Reading mode
                    Text(
                        text = "Reading Mode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                    )

                    ReadingMode.entries.forEach { mode ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = configuration.readingMode == mode,
                                onClick = {
                                    onConfigurationChanged(
                                        configuration.copy(readingMode = mode),
                                    )
                                },
                            )
                            Text(
                                text = mode.name.replace("_", " "),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }

                item {
                    // Background color
                    Text(
                        text = "Background Color",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                    )

                    ReaderBackgroundColor.entries.forEach { color ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = configuration.backgroundColor == color,
                                onClick = {
                                    onConfigurationChanged(
                                        configuration.copy(backgroundColor = color),
                                    )
                                },
                            )
                            Text(
                                text = color.displayName,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }

                item {
                    // Zoom mode
                    Text(
                        text = "Zoom Mode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                    )

                    ZoomMode.entries.forEach { mode ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = configuration.zoomMode == mode,
                                onClick = {
                                    onConfigurationChanged(
                                        configuration.copy(zoomMode = mode),
                                    )
                                },
                            )
                            Text(
                                text = mode.name.replace("_", " "),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier =
            Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
    )
}

@Preview(showBackground = true)
@Composable
private fun EnhancedReadingScreenPreview() {
    MyriadTheme {
        EnhancedReadingScreen(
            mangaTitle = "Sample Manga",
            chapterTitle = "Chapter 1: The Beginning",
            pages = listOf("page1.jpg", "page2.jpg", "page3.jpg"),
            currentPage = 0,
            onPageChanged = {},
            onBackPress = {},
        )
    }
}
