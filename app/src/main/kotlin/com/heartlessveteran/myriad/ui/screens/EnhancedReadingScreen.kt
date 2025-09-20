package com.heartlessveteran.myriad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.heartlessveteran.myriad.ui.components.DoublePageReader
import com.heartlessveteran.myriad.ui.components.HorizontalReader
import com.heartlessveteran.myriad.ui.components.TapZoneOverlay
import com.heartlessveteran.myriad.ui.components.VerticalReader
import com.heartlessveteran.myriad.ui.components.executeTapAction

/*
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
 * Tap zone configuration for navigation
 */
data class TapZoneConfiguration(
    val leftZoneAction: TapAction = TapAction.PREVIOUS_PAGE,
    val rightZoneAction: TapAction = TapAction.NEXT_PAGE,
    val centerZoneAction: TapAction = TapAction.TOGGLE_MENU,
    val topZoneAction: TapAction = TapAction.TOGGLE_MENU,
    val bottomZoneAction: TapAction = TapAction.TOGGLE_MENU,
    val enableTapZones: Boolean = true,
    val leftZoneWidth: Float = 0.33f, // Percentage of screen width
    val rightZoneWidth: Float = 0.33f, // Percentage of screen width
    val topZoneHeight: Float = 0.25f, // Percentage of screen height
    val bottomZoneHeight: Float = 0.25f, // Percentage of screen height
)

/**
 * Actions that can be triggered by tap zones
 */
enum class TapAction {
    PREVIOUS_PAGE,
    NEXT_PAGE,
    TOGGLE_MENU,
    NONE,
    ZOOM_IN,
    ZOOM_OUT,
    TOGGLE_BOOKMARK,
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
    val tapZones: TapZoneConfiguration = TapZoneConfiguration(),
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
        // Tap zone overlay with reader content
        TapZoneOverlay(
            configuration = readerConfig.tapZones,
            onTapAction = { action ->
                executeTapAction(
                    action = action,
                    currentPage = currentPage,
                    totalPages = pages.size,
                    onPageChanged = onPageChanged,
                    onMenuToggle = { isMenuVisible = !isMenuVisible },
                    onBookmarkToggle = { /* TODO: Implement bookmark toggle */ },
                    onZoomIn = {
                        if (scale < 3f) scale += 0.5f
                    },
                    onZoomOut = {
                        if (scale > 0.5f) scale -= 0.5f
                    },
                )
            },
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

            // Menu overlay with simple UI
            if (isMenuVisible) {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shadowElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = onBackPress) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                        }

                        // Page indicator
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
                                onValueChange = { onPageChanged(it.toInt()) },
                                valueRange = 0f..(pages.size - 1).toFloat(),
                                steps = if (pages.size > 2) pages.size - 2 else 0,
                                modifier = Modifier.weight(1f),
                            )

                            Text(
                                text = "${pages.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(40.dp),
                            )
                        }
                    }
                }
            }
        }

        // Settings dialog
        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                title = { Text("Reader Settings") },
                text = {
                    Column {
                        Text("Reading Mode")
                        // Add settings controls here
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettings = false }) {
                        Text("Done")
                    }
                },
            )
        }
    }
}
