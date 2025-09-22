package com.heartlessveteran.myriad.core.domain.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Plugin entity representing a source plugin in the database
 */
@Entity(tableName = "plugins")
data class Plugin(
    @PrimaryKey val id: String,
    val name: String,
    val author: String = "",
    val version: String = "1.0.0",
    val description: String = "",
    val language: String = "en",
    val baseUrl: String = "",
    val isEnabled: Boolean = true,
    val isInstalled: Boolean = true,
    val lastUpdated: Date = Date(),
    val dateInstalled: Date = Date(),
    /**
     * Plugin type - "manga" or "anime"
     */
    val type: PluginType = PluginType.MANGA,
    /**
     * Configuration for the plugin stored as JSON string
     */
    val configuration: String = "{}"
)

/**
 * Type of content the plugin handles
 */
enum class PluginType {
    MANGA,
    ANIME,
    BOTH
}