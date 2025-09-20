package com.heartlessveteran.myriad.domain.sources

import com.heartlessveteran.myriad.domain.models.Result
import kotlinx.coroutines.flow.Flow

/**
 * Core Source interface for the Domain Layer
 * 
 * This is the pure Kotlin domain interface that all sources must implement.
 * Follows Clean Architecture principles with Result-wrapped methods,
 * null-safe design, and full compatibility with MVVM pattern.
 * 
 * This interface provides the foundation for the three-layer extensible source system:
 * - Domain Layer (this interface): Pure business logic contracts
 * - Data Layer: Compatibility implementations (TachiyomiHttpSource, etc.)
 * - App Layer: ExtensionManager for loading and managing sources
 */
interface Source {
    /** Unique identifier for this source */
    val id: String
    
    /** Human-readable name of the source */
    val name: String
    
    /** Base URL for the source (if applicable) */
    val baseUrl: String
    
    /** Language code (e.g., "en", "ja", "es") */
    val language: String
    
    /** Version of the source implementation */
    val version: String
    
    /** Whether this source is currently enabled */
    val isEnabled: Boolean
    
    /** Set of features supported by this source */
    val supportedFeatures: Set<SourceFeature>

    /**
     * Search for content on this source
     *
     * @param query Search term
     * @param page Page number (1-based)
     * @param filters Additional search filters
     * @return Result containing list of content items or error
     */
    suspend fun search(
        query: String,
        page: Int = 1,
        filters: Map<String, String> = emptyMap()
    ): Result<SourcePage<ContentItem>>

    /**
     * Get latest content from this source
     *
     * @param page Page number (1-based)
     * @return Result containing list of latest content or error
     */
    suspend fun getLatest(page: Int = 1): Result<SourcePage<ContentItem>>

    /**
     * Get popular content from this source
     *
     * @param page Page number (1-based)
     * @return Result containing list of popular content or error
     */
    suspend fun getPopular(page: Int = 1): Result<SourcePage<ContentItem>>

    /**
     * Get detailed information about specific content
     *
     * @param contentId Unique identifier for the content
     * @return Result containing detailed content information or error
     */
    suspend fun getContentDetails(contentId: String): Result<ContentDetail>

    /**
     * Get chapters/episodes for specific content
     *
     * @param contentId Unique identifier for the content
     * @return Result containing list of chapters/episodes or error
     */
    suspend fun getChapterList(contentId: String): Result<List<Chapter>>

    /**
     * Get pages/segments for a specific chapter/episode
     *
     * @param chapterId Unique identifier for the chapter
     * @return Result containing list of page URLs or video segments
     */
    suspend fun getPageList(chapterId: String): Result<List<Page>>

    /**
     * Browse content with filters (if supported)
     *
     * @param filters Browse filters (genre, status, etc.)
     * @param page Page number (1-based)
     * @return Result containing filtered content list or error
     */
    suspend fun browse(
        filters: Map<String, String> = emptyMap(),
        page: Int = 1
    ): Result<SourcePage<ContentItem>>

    /**
     * Get available filter options for browsing (if supported)
     *
     * @return Result containing available filters or error
     */
    suspend fun getFilters(): Result<List<SourceFilter>>
}

/**
 * Features that a source can support
 */
enum class SourceFeature {
    /** Source supports searching */
    SEARCH,
    
    /** Source provides latest content */
    LATEST,
    
    /** Source provides popular content */
    POPULAR,
    
    /** Source supports browsing with filters */
    BROWSE_FILTERS,
    
    /** Source supports detailed content information */
    CONTENT_DETAILS,
    
    /** Source provides chapter/episode lists */
    CHAPTER_LIST,
    
    /** Source provides page/segment lists */
    PAGE_LIST,
    
    /** Source requires authentication */
    AUTH_REQUIRED,
    
    /** Source has rate limiting */
    RATE_LIMITED,
    
    /** Source may contain NSFW content */
    NSFW_CONTENT,
    
    /** Source supports offline reading/viewing */
    OFFLINE_SUPPORT
}

/**
 * A page of content with pagination information
 */
data class SourcePage<T>(
    val items: List<T>,
    val hasNextPage: Boolean,
    val currentPage: Int
)

/**
 * Basic content item from search/browse results
 */
data class ContentItem(
    val id: String,
    val title: String,
    val coverUrl: String?,
    val sourceId: String,
    val url: String,
    val type: ContentType = ContentType.MANGA,
    val status: String? = null,
    val rating: Float? = null
)

/**
 * Detailed content information
 */
data class ContentDetail(
    val id: String,
    val title: String,
    val alternativeTitles: List<String> = emptyList(),
    val description: String?,
    val coverUrl: String?,
    val bannerUrl: String?,
    val sourceId: String,
    val url: String,
    val type: ContentType,
    val status: String?,
    val rating: Float?,
    val genres: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val author: String?,
    val artist: String?,
    val publishedYear: Int?,
    val lastUpdated: Long?
)

/**
 * Chapter/Episode information
 */
data class Chapter(
    val id: String,
    val title: String,
    val number: Float,
    val volume: Int? = null,
    val url: String,
    val publishedDate: Long?,
    val scanlator: String? = null
)

/**
 * Page/Segment information
 */
data class Page(
    val index: Int,
    val url: String,
    val imageUrl: String? = null // For manga pages
)

/**
 * Source filter for browsing
 */
sealed class SourceFilter {
    abstract val name: String
    abstract val key: String

    data class TextFilter(
        override val name: String,
        override val key: String,
        val placeholder: String = ""
    ) : SourceFilter()

    data class SelectFilter(
        override val name: String,
        override val key: String,
        val options: List<FilterOption>,
        val selectedIndex: Int = 0
    ) : SourceFilter()

    data class CheckboxFilter(
        override val name: String,
        override val key: String,
        val isChecked: Boolean = false
    ) : SourceFilter()

    data class TriStateFilter(
        override val name: String,
        override val key: String,
        val state: TriState = TriState.IGNORED
    ) : SourceFilter()

    enum class TriState { IGNORED, INCLUDED, EXCLUDED }
}

/**
 * Filter option for select filters
 */
data class FilterOption(
    val name: String,
    val value: String
)

/**
 * Content types supported by sources
 */
enum class ContentType {
    MANGA,
    ANIME,
    NOVEL,
    WEBTOON
}