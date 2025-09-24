package com.heartlessveteran.myriad.core.data.database

import androidx.room.TypeConverter
import com.heartlessveteran.myriad.core.domain.entities.BackgroundColor
import com.heartlessveteran.myriad.core.domain.entities.PageLayout
import com.heartlessveteran.myriad.core.domain.entities.ReadingDirection
import com.heartlessveteran.myriad.core.domain.entities.ZoomType
import java.util.Date

/**
 * Type converters for Room database.
 * Handles conversion between complex types and database storage types.
 * Uses simple string concatenation for string lists to avoid KSP issues.
 */
class DatabaseConverters {

    /**
     * Converts List<String> to delimited string for database storage
     * @param list List of strings to convert
     * @return Delimited string representation
     */
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return list.joinToString(separator = "||")
    }

    /**
     * Converts delimited string to List<String> from database
     * @param data Delimited string from database
     * @return List of strings
     */
    @TypeConverter
    fun toStringList(data: String): List<String> {
        return if (data.isEmpty()) {
            emptyList()
        } else {
            data.split("||")
        }
    }

    /**
     * Converts Date to timestamp for database storage
     * @param date Date object to convert
     * @return Timestamp as Long
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    /**
     * Converts timestamp to Date from database
     * @param timestamp Timestamp from database
     * @return Date object
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    // Reader Settings Enum Converters

    /**
     * Converts ReadingDirection enum to string for database storage
     */
    @TypeConverter
    fun fromReadingDirection(direction: ReadingDirection): String {
        return direction.name
    }

    /**
     * Converts string to ReadingDirection enum from database
     */
    @TypeConverter
    fun toReadingDirection(direction: String): ReadingDirection {
        return ReadingDirection.valueOf(direction)
    }

    /**
     * Converts PageLayout enum to string for database storage
     */
    @TypeConverter
    fun fromPageLayout(layout: PageLayout): String {
        return layout.name
    }

    /**
     * Converts string to PageLayout enum from database
     */
    @TypeConverter
    fun toPageLayout(layout: String): PageLayout {
        return PageLayout.valueOf(layout)
    }

    /**
     * Converts BackgroundColor enum to string for database storage
     */
    @TypeConverter
    fun fromBackgroundColor(color: BackgroundColor): String {
        return color.name
    }

    /**
     * Converts string to BackgroundColor enum from database
     */
    @TypeConverter
    fun toBackgroundColor(color: String): BackgroundColor {
        return BackgroundColor.valueOf(color)
    }

    /**
     * Converts ZoomType enum to string for database storage
     */
    @TypeConverter
    fun fromZoomType(zoomType: ZoomType): String {
        return zoomType.name
    }

    /**
     * Converts string to ZoomType enum from database
     */
    @TypeConverter
    fun toZoomType(zoomType: String): ZoomType {
        return ZoomType.valueOf(zoomType)
    }
}