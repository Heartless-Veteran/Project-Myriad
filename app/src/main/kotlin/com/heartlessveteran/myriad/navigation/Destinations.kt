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
    
    fun validateMangaId(mangaId: String?): Boolean {
        return !mangaId.isNullOrBlank() && mangaId.length <= 255
    }
    
    fun validateAnimeId(animeId: String?): Boolean {
        return !animeId.isNullOrBlank() && animeId.length <= 255
    }
    
    fun validatePage(page: String?): Boolean {
        return page?.toIntOrNull()?.let { it >= 0 } ?: false
    }
    
    fun validateTimestamp(timestamp: String?): Boolean {
        return timestamp?.toLongOrNull()?.let { it >= 0 } ?: false
    }
    
    fun validateSearchQuery(query: String?): Boolean {
        return query?.length?.let { it <= 500 } ?: true // Allow empty queries
    }
    
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