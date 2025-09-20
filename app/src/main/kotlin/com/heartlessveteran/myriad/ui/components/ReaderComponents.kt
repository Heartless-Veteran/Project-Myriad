package com.heartlessveteran.myriad.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.heartlessveteran.myriad.ui.screens.ReaderConfiguration
import com.heartlessveteran.myriad.ui.screens.ReadingMode
import kotlin.math.absoluteValue

/**
 * Horizontal reader component for left-to-right and right-to-left reading modes.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalReader(
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
    val pagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = { pages.size }
    )
    
    // Update current page when pager state changes
    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }
    
    // Update pager when external current page changes
    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage) {
            pagerState.animateScrollToPage(currentPage)
        }
    }
    
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        reverseLayout = configuration.readingMode == ReadingMode.RIGHT_TO_LEFT
    ) { pageIndex ->
        PageContent(
            imageUrl = pages[pageIndex],
            configuration = configuration,
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY,
            onScaleChange = onScaleChange,
            onOffsetChange = onOffsetChange,
            onMenuToggle = onMenuToggle,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Vertical reader component for vertical and webtoon reading modes.
 */
@Composable
fun VerticalReader(
    pages: List<String>,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    configuration: ReaderConfiguration,
    onMenuToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentPage)
    
    // Update current page when scroll position changes
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val visibleItemIndex = listState.firstVisibleItemIndex
            if (visibleItemIndex != currentPage) {
                onPageChanged(visibleItemIndex)
            }
        }
    }
    
    // Update scroll position when external current page changes
    LaunchedEffect(currentPage) {
        if (listState.firstVisibleItemIndex != currentPage) {
            listState.animateScrollToItem(currentPage)
        }
    }
    
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = if (configuration.readingMode == com.heartlessveteran.myriad.ui.screens.ReadingMode.WEBTOON) {
            Arrangement.spacedBy(0.dp)
        } else {
            Arrangement.spacedBy(4.dp)
        }
    ) {
        items(pages.size) { pageIndex ->
            PageContent(
                imageUrl = pages[pageIndex],
                configuration = configuration,
                scale = 1f, // Fixed scale for vertical mode
                offsetX = 0f,
                offsetY = 0f,
                onScaleChange = {},
                onOffsetChange = { _, _ -> },
                onMenuToggle = onMenuToggle,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (configuration.readingMode == com.heartlessveteran.myriad.ui.screens.ReadingMode.WEBTOON) {
                            Modifier.wrapContentHeight()
                        } else {
                            Modifier.fillParentMaxHeight()
                        }
                    )
            )
        }
    }
}

/**
 * Double-page reader component for displaying two pages side by side.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DoublePageReader(
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
    // Group pages into pairs for double-page display
    val pagedItems = remember(pages) {
        pages.chunked(2).mapIndexed { index, pageGroup ->
            Pair(index * 2, pageGroup)
        }
    }
    
    val pagerState = rememberPagerState(
        initialPage = currentPage / 2,
        pageCount = { pagedItems.size }
    )
    
    // Update current page when pager state changes
    LaunchedEffect(pagerState.currentPage) {
        val newPage = pagerState.currentPage * 2
        if (newPage != currentPage) {
            onPageChanged(newPage)
        }
    }
    
    // Update pager when external current page changes
    LaunchedEffect(currentPage) {
        val targetPage = currentPage / 2
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }
    
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        reverseLayout = configuration.readingMode == com.heartlessveteran.myriad.ui.screens.ReadingMode.RIGHT_TO_LEFT
    ) { pageIndex ->
        val (startIndex, pageGroup) = pagedItems[pageIndex]
        
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center
        ) {
            // First page of the pair
            PageContent(
                imageUrl = pageGroup[0],
                configuration = configuration,
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY,
                onScaleChange = onScaleChange,
                onOffsetChange = onOffsetChange,
                onMenuToggle = onMenuToggle,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            
            // Second page of the pair (if exists)
            if (pageGroup.size > 1) {
                PageContent(
                    imageUrl = pageGroup[1],
                    configuration = configuration,
                    scale = scale,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    onScaleChange = onScaleChange,
                    onOffsetChange = onOffsetChange,
                    onMenuToggle = onMenuToggle,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            } else {
                // Empty space for odd number of pages
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(configuration.backgroundColor.color)
                )
            }
        }
    }
}

/**
 * Individual page content with zoom and pan capabilities.
 */
@Composable
private fun PageContent(
    imageUrl: String,
    configuration: ReaderConfiguration,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Float, Float) -> Unit,
    onMenuToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .background(configuration.backgroundColor.color)
            .pointerInput(configuration.enableGestures) {
                if (configuration.enableGestures) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(0.5f, 3f)
                        onScaleChange(newScale)
                        
                        val newOffsetX = offsetX + pan.x
                        val newOffsetY = offsetY + pan.y
                        onOffsetChange(newOffsetX, newOffsetY)
                    }
                }
            }
            .pointerInput(configuration.enableDoubleTapZoom) {
                if (configuration.enableDoubleTapZoom) {
                    detectTapGestures(
                        onDoubleTap = {
                            val newScale = if (scale > 1f) 1f else 2f
                            onScaleChange(newScale)
                            if (newScale == 1f) {
                                onOffsetChange(0f, 0f)
                            }
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Manga page",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentScale = when (configuration.zoomMode) {
                com.heartlessveteran.myriad.ui.screens.ZoomMode.FIT_WIDTH -> ContentScale.FillWidth
                com.heartlessveteran.myriad.ui.screens.ZoomMode.FIT_HEIGHT -> ContentScale.FillHeight
                com.heartlessveteran.myriad.ui.screens.ZoomMode.FIT_SCREEN -> ContentScale.Fit
                com.heartlessveteran.myriad.ui.screens.ZoomMode.ORIGINAL_SIZE -> ContentScale.None
                com.heartlessveteran.myriad.ui.screens.ZoomMode.CUSTOM -> ContentScale.Crop
            }
        )
        
        // Loading indicator
        if (imageUrl.isBlank()) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}