package com.projectmyriad.domain.entities

import kotlinx.serialization.Serializable

/**
 * Core Anime entity representing anime content in the system.
 */
@Serializable
data class Anime(
    val id: String,
    val title: String,
    val studio: String,
    val description: String,
    val coverImage: String,
    val episodes: List<AnimeEpisode>,
    val genres: List<String>,
    val status: AnimeStatus,
    val rating: Float,
    val tags: List<String>,
    // Enhanced properties
    val alternativeTitles: List<String> = emptyList(),
    val originalLanguage: String? = null,
    val releaseYear: Int? = null,
    val contentRating: ContentRating? = null,
    val themes: List<String> = emptyList(),
    val demographics: List<String> = emptyList(),
    val source: String? = null,
    val sourceUrl: String? = null,
    val lastUpdated: String? = null,
    val totalSize: Long? = null,
    val favorited: Boolean = false,
    val personalRating: Float? = null,
    val notes: String? = null,
    val watchProgress: Float = 0f,
    val currentEpisode: Int = 0
)

@Serializable
enum class AnimeStatus {
    AIRING,
    COMPLETED,
    NOT_YET_AIRED,
    CANCELLED
}