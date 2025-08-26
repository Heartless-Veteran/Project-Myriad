package com.heartlessveteran.myriad.navigation

import android.net.Uri
import androidx.navigation.NavController
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations using sealed classes
 */
@Serializable
sealed class Destination {
    
    @Serializable
    data object Home : Destination()
    
    @Serializable
    data object MangaLibrary : Destination()
    
    @Serializable
    data object AnimeLibrary : Destination()
    
    @Serializable
    data object Browse : Destination()
    
    @Serializable
    data object AICore : Destination()
    
    @Serializable
    data class Reading(
        val mangaId: String,
        val chapterId: String? = null,
        val page: Int = 0
    ) : Destination() {
        
        companion object {
            /**
             * Build a navigation route for the Reading destination.
             *
             * The returned route is of the form:
             * - "reading/{encodedMangaId}/{encodedChapterId}?page={page}" when `chapterId` is provided, or
             * - "reading/{encodedMangaId}?page={page}" when `chapterId` is null.
             *
             * Both `mangaId` and `chapterId` (if provided) are URI-encoded.
             *
             * @param mangaId The manga identifier; will be URI-encoded and placed in the path.
             * @param chapterId Optional chapter identifier; if provided it will be URI-encoded and included in the path.
             * @param page Zero-based page index to include as the `page` query parameter (default 0).
             * @return A route string suitable for navigation to the Reading destination.
             */
            fun createRoute(mangaId: String, chapterId: String? = null, page: Int = 0): String {
                val encodedMangaId = Uri.encode(mangaId)
                val encodedChapterId = chapterId?.let { Uri.encode(it) }
                return if (encodedChapterId != null) {
                    "reading/$encodedMangaId/$encodedChapterId?page=$page"
                } else {
                    "reading/$encodedMangaId?page=$page"
                }
            }
        }
    }
    
    @Serializable
    data class Watching(
        val animeId: String,
        val episodeId: String? = null,
        val timestamp: Long = 0
    ) : Destination() {
        
        companion object {
            /**
             * Build a navigation route for the Watching destination.
             *
             * Encodes provided IDs for safe inclusion in the route and appends the timestamp as a query parameter.
             *
             * @param animeId The anime identifier (required); will be URI-encoded.
             * @param episodeId Optional episode identifier; if provided it will be URI-encoded and included in the path.
             * @param timestamp Playback timestamp to include as the `timestamp` query parameter (default 0).
             * @return A route string like `watching/{animeId}/{episodeId}?timestamp={timestamp}` or `watching/{animeId}?timestamp={timestamp}` when `episodeId` is null.
             */
            fun createRoute(animeId: String, episodeId: String? = null, timestamp: Long = 0): String {
                val encodedAnimeId = Uri.encode(animeId)
                val encodedEpisodeId = episodeId?.let { Uri.encode(it) }
                return if (encodedEpisodeId != null) {
                    "watching/$encodedAnimeId/$encodedEpisodeId?timestamp=$timestamp"
                } else {
                    "watching/$encodedAnimeId?timestamp=$timestamp"
                }
            }
        }
    }
    
    @Serializable
    data class MangaDetail(
        val mangaId: String,
        val sourceId: String? = null
    ) : Destination() {
        
        companion object {
            /**
             * Builds the navigation route for the MangaDetail destination.
             *
             * Both `mangaId` and `sourceId` (when provided) are URL-encoded and inserted into the route.
             *
             * @param mangaId The manga identifier to include in the path; will be URL-encoded.
             * @param sourceId Optional source identifier to include as the `sourceId` query parameter; will be URL-encoded if present.
             * @return A route string of the form `manga_detail/{encodedMangaId}` with an optional `?sourceId={encodedSourceId}` query parameter.
             */
            fun createRoute(mangaId: String, sourceId: String? = null): String {
                val encodedMangaId = Uri.encode(mangaId)
                return if (sourceId != null) {
                    "manga_detail/$encodedMangaId?sourceId=${Uri.encode(sourceId)}"
                } else {
                    "manga_detail/$encodedMangaId"
                }
            }
        }
    }
    
    @Serializable
    data class AnimeDetail(
        val animeId: String,
        val sourceId: String? = null
    ) : Destination() {
        
        companion object {
            /**
             * Builds the navigation route for the AnimeDetail destination.
             *
             * The provided IDs are URI-encoded. If `sourceId` is non-null it is appended as
             * a `sourceId` query parameter.
             *
             * @param animeId The raw anime identifier to navigate to; will be URI-encoded.
             * @param sourceId Optional source identifier included as `?sourceId=...` when present.
             * @return A route string like `anime_detail/{encodedAnimeId}` or
             * `anime_detail/{encodedAnimeId}?sourceId={encodedSourceId}`.
             */
            fun createRoute(animeId: String, sourceId: String? = null): String {
                val encodedAnimeId = Uri.encode(animeId)
                return if (sourceId != null) {
                    "anime_detail/$encodedAnimeId?sourceId=${Uri.encode(sourceId)}"
                } else {
                    "anime_detail/$encodedAnimeId"
                }
            }
        }
    }
    
    @Serializable
    data class Search(
        val query: String = "",
        val type: ContentType = ContentType.ALL,
        val source: String? = null
    ) : Destination() {
        
        companion object {
            /**
             * Build a navigation route string for the Search destination.
             *
             * The `query` is URL-encoded when non-empty. The `type` is always included using its enum name.
             * If `source` is provided it will be URL-encoded and appended as a query parameter.
             *
             * @param query Search text; when empty an empty `query` parameter is produced.
             * @param type Content type to restrict the search (uses `type.name`).
             * @param source Optional source identifier to filter results.
             * @return A route string of the form `search?query={encodedQuery}&type={TYPE}` with an optional `&source={encodedSource}`.
             */
            fun createRoute(query: String = "", type: ContentType = ContentType.ALL, source: String? = null): String {
                val encodedQuery = if (query.isNotEmpty()) Uri.encode(query) else ""
                return if (source != null) {
                    "search?query=$encodedQuery&type=${type.name}&source=${Uri.encode(source)}"
                } else {
                    "search?query=$encodedQuery&type=${type.name}"
                }
            }
        }
    }
    
    @Serializable
    data class Settings(
        val section: SettingsSection = SettingsSection.GENERAL
    ) : Destination() {
        
        companion object {
            /**
             * Builds a navigation route for the given settings section.
             *
             * @param section The settings section to navigate to; defaults to GENERAL.
             * @return The route path "settings/{section}" where `{section}` is the section name in lowercase.
             */
            fun createRoute(section: SettingsSection = SettingsSection.GENERAL): String {
                return "settings/${section.name.lowercase()}"
            }
        }
    }
}

/**
 * Content type for search and filtering
 */
enum class ContentType {
    ALL, MANGA, ANIME
}

/**
 * Settings sections
 */
enum class SettingsSection {
    GENERAL, READING, WATCHING, SOURCES, STORAGE, AI, ABOUT
}

/**
 * Navigation route patterns for NavHost
 */
object NavigationRoutes {
    const val HOME = "home"
    const val MANGA_LIBRARY = "manga_library"
    const val ANIME_LIBRARY = "anime_library"
    const val BROWSE = "browse"
    const val AI_CORE = "ai_core"
    const val READING = "reading/{mangaId}?chapterId={chapterId}&page={page}"
    const val WATCHING = "watching/{animeId}?episodeId={episodeId}&timestamp={timestamp}"
    const val MANGA_DETAIL = "manga_detail/{mangaId}?sourceId={sourceId}"
    const val ANIME_DETAIL = "anime_detail/{animeId}?sourceId={sourceId}"
    const val SEARCH = "search?query={query}&type={type}&source={source}"
    const val SETTINGS = "settings/{section}"
}

/**
 * Navigation parameter keys
 */
object NavigationParams {
    const val MANGA_ID = "mangaId"
    const val ANIME_ID = "animeId"
    const val CHAPTER_ID = "chapterId"
    const val EPISODE_ID = "episodeId"
    const val SOURCE_ID = "sourceId"
    const val PAGE = "page"
    const val TIMESTAMP = "timestamp"
    const val QUERY = "query"
    const val TYPE = "type"
    const val SOURCE = "source"
    const val SECTION = "section"
}

/**
 * Deep link patterns for external navigation
 */
object DeepLinks {
    const val SCHEME = "myriad"
    const val HOST = "app"
    
    const val MANGA_DETAIL = "$SCHEME://$HOST/manga/{mangaId}"
    const val ANIME_DETAIL = "$SCHEME://$HOST/anime/{animeId}"
    const val READING = "$SCHEME://$HOST/read/{mangaId}/{chapterId}"
    const val WATCHING = "$SCHEME://$HOST/watch/{animeId}/{episodeId}"
    const val SEARCH = "$SCHEME://$HOST/search?q={query}"
}

/**
 * Navigation validation
 */
object NavigationValidator {
    
    /**
     * Returns true when the provided mangaId is non-null, non-blank, and no longer than 255 characters.
     *
     * @param mangaId The manga identifier to validate.
     * @return `true` if `mangaId` is non-null, not blank, and its length is <= 255; otherwise `false`.
     */
    fun validateMangaId(mangaId: String?): Boolean {
        return !mangaId.isNullOrBlank() && mangaId.length <= 255
    }
    
    /**
     * Validates an anime identifier for navigation use.
     *
     * Returns true if the provided `animeId` is non-null, not blank, and does not exceed 255 characters.
     *
     * @param animeId The anime identifier to validate.
     * @return `true` when `animeId` is non-null, non-blank, and length <= 255; otherwise `false`.
     */
    fun validateAnimeId(animeId: String?): Boolean {
        return !animeId.isNullOrBlank() && animeId.length <= 255
    }
    
    /**
     * Validates that a page query parameter represents a non-negative integer.
     *
     * Returns true if [page] is a non-null string that can be parsed to an integer >= 0; otherwise false.
     *
     * @param page The page value as a string (may be null).
     * @return True when [page] parses to an integer >= 0, false otherwise.
     */
    fun validatePage(page: String?): Boolean {
        return page?.toIntOrNull()?.let { it >= 0 } ?: false
    }
    
    /**
     * Validates that a timestamp string represents a non-negative millisecond value.
     *
     * @param timestamp A nullable string expected to contain a numeric timestamp (milliseconds). Null, blank, or non-numeric values are considered invalid.
     * @return `true` if `timestamp` can be parsed as a `Long` and is >= 0; otherwise `false`.
     */
    fun validateTimestamp(timestamp: String?): Boolean {
        return timestamp?.toLongOrNull()?.let { it >= 0 } ?: false
    }
    
    /**
     * Validates a search query string.
     *
     * Returns true when the query is null (no query) or its length is at most 500 characters.
     *
     * @param query The search query to validate; may be null.
     * @return True if the query is null or length <= 500, false otherwise.
     */
    fun validateSearchQuery(query: String?): Boolean {
        return query?.length?.let { it <= 500 } ?: true // Allow empty queries
    }
    
    /**
     * Validates whether the provided string represents a valid ContentType.
     *
     * Accepts null (treated as valid). If non-null, the value is compared case-insensitively
     * against the ContentType enum names (e.g. "ALL", "MANGA", "ANIME").
     *
     * @param type The string to validate; may be null.
     * @return `true` if `type` is null or matches one of the ContentType enum names (case-insensitive); `false` otherwise.
     */
    fun validateContentType(type: String?): Boolean {
        return type?.let { 
            try {
                ContentType.valueOf(it.uppercase())
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        } ?: true // Allow null
    }
    
    /**
     * Validates whether a provided settings section name corresponds to a known SettingsSection.
     *
     * The check is case-insensitive and treats `null` as valid (used when no specific section is provided).
     *
     * @param section The settings section name to validate (may be null).
     * @return `true` if `section` is `null` or matches any SettingsSection name (ignoring case); `false` otherwise.
     */
    fun validateSettingsSection(section: String?): Boolean {
        return section?.let {
            try {
                SettingsSection.valueOf(it.uppercase())
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        } ?: true // Allow null
    }
}