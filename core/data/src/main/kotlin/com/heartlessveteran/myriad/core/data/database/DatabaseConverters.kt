package com.heartlessveteran.myriad.core.data.database

import androidx.room.TypeConverter
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
}