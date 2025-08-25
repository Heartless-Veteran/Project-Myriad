package com.projectmyriad.data.database.converters

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room type converters for complex data types.
 * Handles JSON serialization/deserialization for lists and complex objects.
 */
class Converters {
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            json.decodeFromString(value)
        }
    }
    
    @TypeConverter
    fun fromLong(value: Long?): Long? {
        return value
    }
    
    @TypeConverter
    fun toLong(value: Long?): Long? {
        return value
    }
}