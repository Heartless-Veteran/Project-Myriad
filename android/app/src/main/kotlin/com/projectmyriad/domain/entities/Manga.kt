package com.projectmyriad.domain.entities

import kotlinx.serialization.Serializable

/**
 * Core Manga entity representing manga content in the system.
 * This follows the Clean Architecture Domain layer principles.
 */
@Serializable
data class Manga(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val coverImage: String,
    val chapters: List<MangaChapter>,
    val genres: List<String>,
    val status: MangaStatus,
    val rating: Float,
    val tags: List<String>,
    // Enhanced properties for Phase 2
    val alternativeTitles: List<String> = emptyList(),
    val originalLanguage: String? = null,
    val publicationYear: Int? = null,
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
    val readingProgress: Float = 0f
)

@Serializable
enum class MangaStatus {
    ONGOING,
    COMPLETED,
    HIATUS
}

@Serializable
enum class ContentRating {
    G,
    PG,
    PG_13,
    R,
    NC_17
}