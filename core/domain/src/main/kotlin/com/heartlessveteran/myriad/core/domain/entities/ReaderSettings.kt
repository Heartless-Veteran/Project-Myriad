package com.heartlessveteran.myriad.core.domain.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Reader settings entity for customizable manga reading experience.
 * Follows the requirement for customizable reader with reading direction,
 * background, zoom, and scaling options.
 */
@Entity(tableName = "reader_settings")
data class ReaderSettings(
    @PrimaryKey val id: String = "default",
    val readingDirection: ReadingDirection = ReadingDirection.LEFT_TO_RIGHT,
    val pageLayout: PageLayout = PageLayout.SINGLE_PAGE,
    val backgroundColor: BackgroundColor = BackgroundColor.BLACK,
    val zoomType: ZoomType = ZoomType.FIT_WIDTH,
    val customZoomLevel: Float = 1f,
    val pageSpacing: Int = 8,
    val enableVolumeKeyNavigation: Boolean = true,
    val enableTapNavigation: Boolean = true,
    val fullscreenMode: Boolean = true,
    val keepScreenOn: Boolean = true,
    val showPageIndicator: Boolean = true,
    val enableDoubleTapZoom: Boolean = true,
    val cropBorders: Boolean = false,
    val animationDuration: Int = 300,
)

/**
 * Reading direction options for manga reader
 */
enum class ReadingDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    VERTICAL,
    WEBTOON
}

/**
 * Page layout options for manga reader
 */
enum class PageLayout {
    SINGLE_PAGE,
    DOUBLE_PAGE,
    AUTOMATIC
}

/**
 * Background color options for manga reader
 */
enum class BackgroundColor {
    BLACK,
    WHITE,
    GRAY,
    AUTOMATIC
}

/**
 * Zoom type options for manga reader
 */
enum class ZoomType {
    FIT_WIDTH,
    FIT_HEIGHT,
    FIT_SCREEN,
    ORIGINAL_SIZE,
    CUSTOM
}