package com.heartlessveteran.myriad.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import com.heartlessveteran.myriad.ui.screens.TapAction
import com.heartlessveteran.myriad.ui.screens.TapZoneConfiguration

/**
 * Tap zone overlay for reader navigation.
 *
 * This component provides customizable tap zones that can trigger different actions
 * based on where the user taps on the screen. Useful for page navigation in manga readers.
 */

/**
 * Represents a tap zone area and its associated action
 */
data class TapZone(
    val action: TapAction,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
) {
    fun contains(offset: Offset): Boolean =
        offset.x >= x &&
            offset.x <= x + width &&
            offset.y >= y &&
            offset.y <= y + height
}

@Composable
fun TapZoneOverlay(
    configuration: TapZoneConfiguration,
    onTapAction: (TapAction) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    // Calculate tap zones based on screen size and configuration
    val tapZones =
        remember(screenSize, configuration) {
            if (screenSize.width == 0 || screenSize.height == 0) {
                emptyList()
            } else {
                calculateTapZones(screenSize, configuration)
            }
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .onSizeChanged { size -> screenSize = size }
                .pointerInput(configuration.enableTapZones, tapZones) {
                    if (configuration.enableTapZones) {
                        detectTapGestures { offset ->
                            // Find which zone was tapped
                            val tappedZone =
                                tapZones.firstOrNull { zone ->
                                    zone.contains(offset)
                                }

                            // Execute the action for the tapped zone
                            tappedZone?.let { zone ->
                                onTapAction(zone.action)
                            }
                        }
                    }
                },
    ) {
        content()
    }
}

/**
 * Calculate tap zones based on screen size and configuration
 */
private fun calculateTapZones(
    screenSize: IntSize,
    configuration: TapZoneConfiguration,
): List<TapZone> {
    val screenWidth = screenSize.width.toFloat()
    val screenHeight = screenSize.height.toFloat()

    val zones = mutableListOf<TapZone>()

    // Calculate zone dimensions
    val leftZoneWidth = screenWidth * configuration.leftZoneWidth
    val rightZoneWidth = screenWidth * configuration.rightZoneWidth
    val topZoneHeight = screenHeight * configuration.topZoneHeight
    val bottomZoneHeight = screenHeight * configuration.bottomZoneHeight

    // Center zone dimensions (remaining area)
    val centerZoneX = leftZoneWidth
    val centerZoneY = topZoneHeight
    val centerZoneWidth = screenWidth - leftZoneWidth - rightZoneWidth
    val centerZoneHeight = screenHeight - topZoneHeight - bottomZoneHeight

    // Left zone (full height, left side)
    if (configuration.leftZoneAction != TapAction.NONE && leftZoneWidth > 0) {
        zones.add(
            TapZone(
                action = configuration.leftZoneAction,
                x = 0f,
                y = 0f,
                width = leftZoneWidth,
                height = screenHeight,
            ),
        )
    }

    // Right zone (full height, right side)
    if (configuration.rightZoneAction != TapAction.NONE && rightZoneWidth > 0) {
        zones.add(
            TapZone(
                action = configuration.rightZoneAction,
                x = screenWidth - rightZoneWidth,
                y = 0f,
                width = rightZoneWidth,
                height = screenHeight,
            ),
        )
    }

    // Top zone (center area, top)
    if (configuration.topZoneAction != TapAction.NONE && topZoneHeight > 0 && centerZoneWidth > 0) {
        zones.add(
            TapZone(
                action = configuration.topZoneAction,
                x = centerZoneX,
                y = 0f,
                width = centerZoneWidth,
                height = topZoneHeight,
            ),
        )
    }

    // Bottom zone (center area, bottom)
    if (configuration.bottomZoneAction != TapAction.NONE && bottomZoneHeight > 0 && centerZoneWidth > 0) {
        zones.add(
            TapZone(
                action = configuration.bottomZoneAction,
                x = centerZoneX,
                y = screenHeight - bottomZoneHeight,
                width = centerZoneWidth,
                height = bottomZoneHeight,
            ),
        )
    }

    // Center zone (remaining area)
    if (configuration.centerZoneAction != TapAction.NONE && centerZoneWidth > 0 && centerZoneHeight > 0) {
        zones.add(
            TapZone(
                action = configuration.centerZoneAction,
                x = centerZoneX,
                y = centerZoneY,
                width = centerZoneWidth,
                height = centerZoneHeight,
            ),
        )
    }

    return zones
}

/**
 * Helper function to execute tap actions
 */
fun executeTapAction(
    action: TapAction,
    currentPage: Int,
    totalPages: Int,
    onPageChanged: (Int) -> Unit,
    onMenuToggle: () -> Unit,
    onBookmarkToggle: () -> Unit = {},
    onZoomIn: () -> Unit = {},
    onZoomOut: () -> Unit = {},
) {
    when (action) {
        TapAction.PREVIOUS_PAGE -> {
            if (currentPage > 0) {
                onPageChanged(currentPage - 1)
            }
        }
        TapAction.NEXT_PAGE -> {
            if (currentPage < totalPages - 1) {
                onPageChanged(currentPage + 1)
            }
        }
        TapAction.TOGGLE_MENU -> {
            onMenuToggle()
        }
        TapAction.TOGGLE_BOOKMARK -> {
            onBookmarkToggle()
        }
        TapAction.ZOOM_IN -> {
            onZoomIn()
        }
        TapAction.ZOOM_OUT -> {
            onZoomOut()
        }
        TapAction.NONE -> {
            // Do nothing
        }
    }
}
