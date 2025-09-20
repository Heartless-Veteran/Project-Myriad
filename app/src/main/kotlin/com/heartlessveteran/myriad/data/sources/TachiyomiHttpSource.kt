package com.heartlessveteran.myriad.data.sources

import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.sources.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Tachiyomi-compatible HTTP source base class
 * 
 * This provides API compatibility (not binary) with Tachiyomi's ParsedHttpSource
 * to allow minimal-effort porting of community extensions. Extensions must be
 * recompiled against Project Myriad's SDK for security and stability.
 * 
 * Key differences from Tachiyomi:
 * - Uses Result wrapper for error handling
 * - Kotlin coroutines instead of RxJava
 * - Null-safe API design
 * - Clean Architecture compliance
 */
abstract class TachiyomiHttpSource : Source {
    
    /** OkHttpClient for network requests */
    protected open val client: OkHttpClient = OkHttpClient()
    
    /** Default headers for requests */
    protected open val headers = mutableMapOf<String, String>()
    
    /** Whether this source supports latest updates */
    protected open val supportsLatest: Boolean = true
    
    // Abstract methods that must be implemented by extensions
    
    /**
     * Build request for searching manga
     */
    protected abstract fun searchMangaRequest(page: Int, query: String, filters: List<SourceFilter>): Request
    
    /**
     * Parse search results from HTML
     */
    protected abstract fun searchMangaParse(response: Response): SourcePage<ContentItem>
    
    /**
     * Build request for latest manga
     */
    protected abstract fun latestUpdatesRequest(page: Int): Request
    
    /**
     * Parse latest updates from HTML
     */
    protected abstract fun latestUpdatesParse(response: Response): SourcePage<ContentItem>
    
    /**
     * Build request for popular manga
     */
    protected abstract fun popularMangaRequest(page: Int): Request
    
    /**
     * Parse popular manga from HTML
     */
    protected abstract fun popularMangaParse(response: Response): SourcePage<ContentItem>
    
    /**
     * Build request for manga details
     */
    protected abstract fun mangaDetailsRequest(manga: ContentItem): Request
    
    /**
     * Parse manga details from HTML
     */
    protected abstract fun mangaDetailsParse(response: Response): ContentDetail
    
    /**
     * Build request for chapter list
     */
    protected abstract fun chapterListRequest(manga: ContentDetail): Request
    
    /**
     * Parse chapter list from HTML
     */
    protected abstract fun chapterListParse(response: Response): List<Chapter>
    
    /**
     * Build request for page list
     */
    protected abstract fun pageListRequest(chapter: Chapter): Request
    
    /**
     * Parse page list from HTML
     */
    protected abstract fun pageListParse(response: Response): List<Page>
    
    /**
     * Get image request for a page
     */
    protected open fun imageUrlRequest(page: Page): Request = 
        Request.Builder().url(page.imageUrl ?: page.url).headers(headers.toHeaders()).build()
    
    // Implementation of Source interface using the abstract methods
    
    override suspend fun search(
        query: String,
        page: Int,
        filters: Map<String, String>
    ): Result<SourcePage<ContentItem>> = executeRequest {
        val filterList = parseFilters(filters)
        val request = searchMangaRequest(page, query, filterList)
        val response = client.newCall(request).execute()
        searchMangaParse(response)
    }
    
    override suspend fun getLatest(page: Int): Result<SourcePage<ContentItem>> = 
        if (supportsLatest) {
            executeRequest {
                val request = latestUpdatesRequest(page)
                val response = client.newCall(request).execute()
                latestUpdatesParse(response)
            }
        } else {
            Result.Error(UnsupportedOperationException("Latest updates not supported"))
        }
    
    override suspend fun getPopular(page: Int): Result<SourcePage<ContentItem>> = executeRequest {
        val request = popularMangaRequest(page)
        val response = client.newCall(request).execute()
        popularMangaParse(response)
    }
    
    override suspend fun getContentDetails(contentId: String): Result<ContentDetail> = executeRequest {
        // Create a temporary ContentItem to build the request
        val tempItem = ContentItem(
            id = contentId,
            title = "",
            coverUrl = null,
            sourceId = id,
            url = contentId
        )
        val request = mangaDetailsRequest(tempItem)
        val response = client.newCall(request).execute()
        mangaDetailsParse(response)
    }
    
    override suspend fun getChapterList(contentId: String): Result<List<Chapter>> = executeRequest {
        // Create a temporary ContentDetail to build the request
        val tempDetail = ContentDetail(
            id = contentId,
            title = "",
            alternativeTitles = emptyList(),
            description = null,
            coverUrl = null,
            bannerUrl = null,
            sourceId = id,
            url = contentId,
            type = com.heartlessveteran.myriad.domain.sources.ContentType.MANGA,
            status = null,
            rating = null,
            genres = emptyList(),
            tags = emptyList(),
            author = null,
            artist = null,
            publishedYear = null,
            lastUpdated = null
        )
        val request = chapterListRequest(tempDetail)
        val response = client.newCall(request).execute()
        chapterListParse(response)
    }
    
    override suspend fun getPageList(chapterId: String): Result<List<Page>> = executeRequest {
        // Create a temporary Chapter to build the request
        val tempChapter = Chapter(
            id = chapterId,
            title = "",
            number = 0f,
            url = chapterId,
            publishedDate = null
        )
        val request = pageListRequest(tempChapter)
        val response = client.newCall(request).execute()
        pageListParse(response)
    }
    
    override suspend fun browse(
        filters: Map<String, String>,
        page: Int
    ): Result<SourcePage<ContentItem>> = 
        search("", page, filters) // Default implementation uses search with empty query
    
    override suspend fun getFilters(): Result<List<SourceFilter>> = 
        Result.Success(getFilterList())
    
    // Helper methods for compatibility
    
    /**
     * Get filter list for this source (to be overridden by extensions)
     */
    protected open fun getFilterList(): List<SourceFilter> = emptyList()
    
    /**
     * Parse filters from map to list
     */
    private fun parseFilters(filters: Map<String, String>): List<SourceFilter> {
        val filterList = getFilterList().toMutableList()
        // Apply filter values from the map
        // This is a simplified implementation - real implementation would be more complex
        return filterList
    }
    
    /**
     * Execute a network request with error handling
     */
    private suspend inline fun <T> executeRequest(crossinline block: () -> T): Result<T> {
        return try {
            val result = block()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e, "Network request failed: ${e.message}")
        }
    }
    
    /**
     * Convert headers map to OkHttp Headers
     */
    private fun Map<String, String>.toHeaders(): okhttp3.Headers {
        val builder = okhttp3.Headers.Builder()
        forEach { (key, value) ->
            builder.add(key, value)
        }
        return builder.build()
    }
    
    // Convenience methods for HTML parsing (similar to Tachiyomi)
    
    /**
     * Parse HTML response to Document
     */
    protected fun Response.asJsoup(): Document = Jsoup.parse(body?.string() ?: "")
    
    /**
     * Get text content from element or return empty string
     */
    protected fun Element?.text(): String = this?.text() ?: ""
    
    /**
     * Get attribute value from element or return empty string
     */
    protected fun Element?.attr(name: String): String = this?.attr(name) ?: ""
    
    /**
     * Get absolute URL from relative URL
     */
    protected fun Element?.absUrl(attributeKey: String): String = this?.absUrl(attributeKey) ?: ""
    
    /**
     * Select elements from document
     */
    protected fun Document?.select(cssQuery: String) = this?.select(cssQuery)
    
    /**
     * Select first element from document
     */
    protected fun Document?.selectFirst(cssQuery: String) = this?.selectFirst(cssQuery)
}

/**
 * Example implementation of a simple HTTP source using the TachiyomiHttpSource base
 * This demonstrates how community extensions can be easily ported
 */
class ExampleMangaSource : TachiyomiHttpSource() {
    
    override val id = "example_manga"
    override val name = "Example Manga"
    override val baseUrl = "https://example-manga.com"
    override val language = "en"
    override val version = "1.0.0"
    override val isEnabled = true
    override val supportedFeatures = setOf(
        SourceFeature.SEARCH,
        SourceFeature.LATEST,
        SourceFeature.POPULAR,
        SourceFeature.CONTENT_DETAILS,
        SourceFeature.CHAPTER_LIST,
        SourceFeature.PAGE_LIST
    )
    
    override fun searchMangaRequest(page: Int, query: String, filters: List<SourceFilter>): Request {
        return Request.Builder()
            .url("$baseUrl/search?q=$query&page=$page")
            .build()
    }
    
    override fun searchMangaParse(response: Response): SourcePage<ContentItem> {
        val document = response.asJsoup()
        val items = document.select(".manga-item").map { element ->
            ContentItem(
                id = element.attr("data-id"),
                title = element.select(".title").text(),
                coverUrl = element.select("img").attr("src"),
                sourceId = id,
                url = element.select("a").attr("href"),
                type = ContentType.MANGA
            )
        }
        val hasNextPage = document.selectFirst(".next-page") != null
        return SourcePage(items, hasNextPage, 1)
    }
    
    override fun latestUpdatesRequest(page: Int): Request {
        return Request.Builder()
            .url("$baseUrl/latest?page=$page")
            .build()
    }
    
    override fun latestUpdatesParse(response: Response): SourcePage<ContentItem> {
        // Similar implementation to searchMangaParse
        return searchMangaParse(response)
    }
    
    override fun popularMangaRequest(page: Int): Request {
        return Request.Builder()
            .url("$baseUrl/popular?page=$page")
            .build()
    }
    
    override fun popularMangaParse(response: Response): SourcePage<ContentItem> {
        // Similar implementation to searchMangaParse
        return searchMangaParse(response)
    }
    
    override fun mangaDetailsRequest(manga: ContentItem): Request {
        return Request.Builder()
            .url("$baseUrl/manga/${manga.id}")
            .build()
    }
    
    override fun mangaDetailsParse(response: Response): ContentDetail {
        val document = response.asJsoup()
        return ContentDetail(
            id = document.select("[data-id]").attr("data-id"),
            title = document.select(".manga-title").text(),
            description = document.select(".description").text(),
            coverUrl = document.select(".cover img").attr("src"),
            bannerUrl = null,
            sourceId = id,
            url = response.request.url.toString(),
            type = ContentType.MANGA,
            status = document.select(".status").text(),
            rating = document.select(".rating").text().toFloatOrNull(),
            genres = document.select(".genre").map { it.text() },
            author = document.select(".author").text(),
            artist = document.select(".artist").text(),
            publishedYear = document.select(".year").text().toIntOrNull(),
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    override fun chapterListRequest(manga: ContentDetail): Request {
        return Request.Builder()
            .url("$baseUrl/manga/${manga.id}/chapters")
            .build()
    }
    
    override fun chapterListParse(response: Response): List<Chapter> {
        val document = response.asJsoup()
        return document.select(".chapter").map { element ->
            Chapter(
                id = element.attr("data-id"),
                title = element.select(".chapter-title").text(),
                number = element.select(".chapter-number").text().toFloatOrNull() ?: 0f,
                url = element.select("a").attr("href"),
                publishedDate = null,
                scanlator = element.select(".scanlator").text()
            )
        }
    }
    
    override fun pageListRequest(chapter: Chapter): Request {
        return Request.Builder()
            .url("$baseUrl/chapter/${chapter.id}")
            .build()
    }
    
    override fun pageListParse(response: Response): List<Page> {
        val document = response.asJsoup()
        return document.select(".page img").mapIndexed { index, element ->
            Page(
                index = index,
                url = element.attr("src"),
                imageUrl = element.attr("src")
            )
        }
    }
    
    override fun getFilterList(): List<SourceFilter> {
        return listOf(
            SourceFilter.SelectFilter(
                name = "Genre",
                key = "genre",
                options = listOf(
                    FilterOption("All", ""),
                    FilterOption("Action", "action"),
                    FilterOption("Romance", "romance"),
                    FilterOption("Comedy", "comedy")
                )
            ),
            SourceFilter.TriStateFilter(
                name = "Completed",
                key = "completed"
            )
        )
    }
}