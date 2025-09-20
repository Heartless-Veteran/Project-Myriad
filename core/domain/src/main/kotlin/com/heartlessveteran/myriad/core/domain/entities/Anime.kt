package com.heartlessveteran.myriad.core.domain.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Core anime entity representing an anime series in the database
 */
@Entity(tableName = "anime")
data class Anime(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val alternativeTitles: List<String> = emptyList(),
    val description: String = "",
    val studio: String = "",
    val status: AnimeStatus = AnimeStatus.UNKNOWN,
    val genres: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val coverImageUrl: String? = null,
    val localCoverPath: String? = null,
    val rating: Float = 0f,
    val totalEpisodes: Int = 0,
    val watchedEpisodes: Int = 0,
    val isInLibrary: Boolean = false,
    val isFavorite: Boolean = false,
    val dateAdded: Date = Date(),
    val lastWatchedDate: Date? = null,
    val lastUpdated: Date = Date(),
    val releaseYear: Int? = null,
    val season: AnimeSeason? = null,
    val type: AnimeType = AnimeType.TV,
    val source: String = "local", // local, crunchyroll, etc.
    val sourceId: String? = null,
    val isLocal: Boolean = true,
    val localPath: String? = null,
    val duration: Int = 24, // Episode duration in minutes
)

/**
 * Status of an anime series
 */
enum class AnimeStatus {
    AIRING,
    COMPLETED,
    NOT_YET_AIRED,
    CANCELLED,
    UNKNOWN,
}

/**
 * Season when anime was released
 */
enum class AnimeSeason {
    WINTER,
    SPRING,
    SUMMER,
    FALL,
}

/**
 * Type of anime
 */
enum class AnimeType {
    TV,
    MOVIE,
    OVA,
    ONA,
    SPECIAL,
    MUSIC,
}

/**
 * Individual anime episode entity
 */
@Entity(tableName = "anime_episodes")
data class AnimeEpisode(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val animeId: String,
    val episodeNumber: Int,
    val title: String = "",
    val description: String = "",
    val thumbnailUrl: String? = null,
    val isWatched: Boolean = false,
    val watchProgress: Long = 0L, // Progress in milliseconds
    val duration: Long = 0L, // Duration in milliseconds
    val isDownloaded: Boolean = false,
    val localPath: String? = null,
    val dateAdded: Date = Date(),
    val dateWatched: Date? = null,
    val source: String = "local",
    val sourceEpisodeId: String? = null,
    val airDate: Date? = null,
)
