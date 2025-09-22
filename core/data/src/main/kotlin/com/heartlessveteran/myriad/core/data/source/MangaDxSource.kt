package com.heartlessveteran.myriad.core.data.source

import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.entities.MangaChapter
import com.heartlessveteran.myriad.core.domain.entities.MangaStatus
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.Source
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

/**
 * MangaDex source implementation for online manga access.
 * Implements the MangaDex API v5 for fetching manga content.
 * Includes rate limiting and respectful API usage.
 */
class MangaDxSource : Source {
    override val id = "mangadx"
    override val name = "MangaDX"
    override val lang = "en"
    override val baseUrl = "https://api.mangadx.org"
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    // Rate limiting: MangaDX allows 5 requests per second
    private var lastRequestTime = 0L
    private val rateLimitDelay = 200L // 200ms between requests
    
    /**
     * Enforces rate limiting to respect MangaDX API limits
     */
    private suspend fun enforceRateLimit() {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastRequest = currentTime - lastRequestTime
        
        if (timeSinceLastRequest < rateLimitDelay) {
            delay(rateLimitDelay - timeSinceLastRequest)
        }
        
        lastRequestTime = System.currentTimeMillis()
    }
    
    /**
     * Makes HTTP request to MangaDX API with proper error handling
     */
    private suspend fun makeRequest(url: String): String {
        enforceRateLimit()
        
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Project Myriad v1.0")
            connection.connectTimeout = 10000
            connection.readTimeout = 15000
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                throw Exception("HTTP ${connection.responseCode}: ${connection.responseMessage}")
            }
        } finally {
            connection.disconnect()
        }
    }
    
    override suspend fun getLatestManga(page: Int): Result<List<Manga>> {
        return try {
            val offset = page * 20
            val url = "$baseUrl/manga?limit=20&offset=$offset&order[updatedAt]=desc&includes[]=cover_art&includes[]=author&includes[]=artist"
            
            val response = makeRequest(url)
            val mangaResponse = json.decodeFromString<MangaDxResponse<List<MangaDxManga>>>(response)
            
            val mangaList = mangaResponse.data.map { mangaDxManga ->
                convertToManga(mangaDxManga, mangaResponse.relationships ?: emptyList())
            }
            
            Result.Success(mangaList)
        } catch (e: Exception) {
            Result.Error(e, "Failed to fetch latest manga: ${e.message}")
        }
    }
    
    override suspend fun getPopularManga(page: Int): Result<List<Manga>> {
        return try {
            val offset = page * 20
            val url = "$baseUrl/manga?limit=20&offset=$offset&order[followedCount]=desc&includes[]=cover_art&includes[]=author&includes[]=artist"
            
            val response = makeRequest(url)
            val mangaResponse = json.decodeFromString<MangaDxResponse<List<MangaDxManga>>>(response)
            
            val mangaList = mangaResponse.data.map { mangaDxManga ->
                convertToManga(mangaDxManga, mangaResponse.relationships ?: emptyList())
            }
            
            Result.Success(mangaList)
        } catch (e: Exception) {
            Result.Error(e, "Failed to fetch popular manga: ${e.message}")
        }
    }
    
    override suspend fun searchManga(query: String, page: Int): Result<List<Manga>> {
        return try {
            val offset = page * 20
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "$baseUrl/manga?title=$encodedQuery&limit=20&offset=$offset&includes[]=cover_art&includes[]=author&includes[]=artist"
            
            val response = makeRequest(url)
            val mangaResponse = json.decodeFromString<MangaDxResponse<List<MangaDxManga>>>(response)
            
            val mangaList = mangaResponse.data.map { mangaDxManga ->
                convertToManga(mangaDxManga, mangaResponse.relationships ?: emptyList())
            }
            
            Result.Success(mangaList)
        } catch (e: Exception) {
            Result.Error(e, "Failed to search manga: ${e.message}")
        }
    }
    
    override suspend fun getMangaDetails(url: String): Result<Manga> {
        return try {
            // Extract manga ID from URL
            val mangaId = url.substringAfterLast("/")
            val apiUrl = "$baseUrl/manga/$mangaId?includes[]=cover_art&includes[]=author&includes[]=artist"
            
            val response = makeRequest(apiUrl)
            val mangaResponse = json.decodeFromString<MangaDxResponse<MangaDxManga>>(response)
            
            val manga = convertToManga(mangaResponse.data, mangaResponse.relationships ?: emptyList())
            
            Result.Success(manga)
        } catch (e: Exception) {
            Result.Error(e, "Failed to fetch manga details: ${e.message}")
        }
    }
    
    override suspend fun getChapterList(manga: Manga): Result<List<MangaChapter>> {
        return try {
            val mangaId = manga.sourceId ?: return Result.Error(
                IllegalArgumentException("Invalid manga ID"),
                "Manga ID is required for chapter list"
            )
            
            val url = "$baseUrl/manga/$mangaId/feed?translatedLanguage[]=en&order[chapter]=asc&limit=100"
            
            val response = makeRequest(url)
            val chapterResponse = json.decodeFromString<MangaDxResponse<List<MangaDxChapter>>>(response)
            
            val chapters = chapterResponse.data.mapIndexed { index, mangaDxChapter ->
                convertToChapter(mangaDxChapter, manga.id, index)
            }
            
            Result.Success(chapters)
        } catch (e: Exception) {
            Result.Error(e, "Failed to fetch chapter list: ${e.message}")
        }
    }
    
    override suspend fun getChapterPages(url: String): Result<List<String>> {
        return try {
            // Extract chapter ID from URL
            val chapterId = url.substringAfterLast("/")
            val apiUrl = "$baseUrl/at-home/server/$chapterId"
            
            val response = makeRequest(apiUrl)
            val pageResponse = json.decodeFromString<MangaDxPageResponse>(response)
            
            val baseUrl = pageResponse.baseUrl
            val hash = pageResponse.chapter.hash
            val pages = pageResponse.chapter.data.map { fileName ->
                "$baseUrl/data/$hash/$fileName"
            }
            
            Result.Success(pages)
        } catch (e: Exception) {
            Result.Error(e, "Failed to fetch chapter pages: ${e.message}")
        }
    }
    
    /**
     * Converts MangaDX manga data to internal Manga entity
     */
    private fun convertToManga(mangaDxManga: MangaDxManga, relationships: List<MangaDxRelationship>): Manga {
        val attributes = mangaDxManga.attributes
        val title = attributes.title["en"] ?: attributes.title.values.firstOrNull() ?: "Unknown Title"
        
        // Extract cover art URL
        val coverRelationship = relationships.find { it.type == "cover_art" }
        val coverImageUrl = coverRelationship?.let { cover ->
            "https://uploads.mangadx.org/covers/${mangaDxManga.id}/${cover.attributes?.fileName}"
        }
        
        // Extract author and artist
        val author = relationships.find { it.type == "author" }?.attributes?.name ?: ""
        val artist = relationships.find { it.type == "artist" }?.attributes?.name ?: author
        
        return Manga(
            id = UUID.randomUUID().toString(),
            title = title,
            alternativeTitles = attributes.altTitles.flatMap { it.values },
            description = attributes.description["en"] ?: "",
            author = author,
            artist = artist,
            status = when (attributes.status) {
                "ongoing" -> MangaStatus.ONGOING
                "completed" -> MangaStatus.COMPLETED
                "hiatus" -> MangaStatus.HIATUS
                "cancelled" -> MangaStatus.CANCELLED
                else -> MangaStatus.UNKNOWN
            },
            genres = attributes.tags.map { it.attributes.name["en"] ?: "Unknown" },
            coverImageUrl = coverImageUrl,
            source = "mangadx",
            sourceId = mangaDxManga.id,
            isLocal = false
        )
    }
    
    /**
     * Converts MangaDX chapter data to internal MangaChapter entity
     */
    private fun convertToChapter(mangaDxChapter: MangaDxChapter, mangaId: String, index: Int): MangaChapter {
        val attributes = mangaDxChapter.attributes
        val chapterNumber = attributes.chapter?.toFloatOrNull() ?: (index + 1).toFloat()
        
        return MangaChapter(
            id = UUID.randomUUID().toString(),
            mangaId = mangaId,
            chapterNumber = chapterNumber,
            title = attributes.title ?: "Chapter $chapterNumber",
            source = "mangadx",
            sourceChapterId = mangaDxChapter.id,
            dateAdded = Date()
        )
    }
}

// Data models for MangaDX API responses
@Serializable
data class MangaDxResponse<T>(
    val data: T,
    val relationships: List<MangaDxRelationship>? = null
)

@Serializable
data class MangaDxManga(
    val id: String,
    val attributes: MangaDxMangaAttributes
)

@Serializable
data class MangaDxMangaAttributes(
    val title: Map<String, String>,
    val altTitles: List<Map<String, String>> = emptyList(),
    val description: Map<String, String> = emptyMap(),
    val status: String = "unknown",
    val tags: List<MangaDxTag> = emptyList()
)

@Serializable
data class MangaDxTag(
    val attributes: MangaDxTagAttributes
)

@Serializable
data class MangaDxTagAttributes(
    val name: Map<String, String>
)

@Serializable
data class MangaDxChapter(
    val id: String,
    val attributes: MangaDxChapterAttributes
)

@Serializable
data class MangaDxChapterAttributes(
    val title: String? = null,
    val chapter: String? = null,
    val pages: Int = 0
)

@Serializable
data class MangaDxRelationship(
    val type: String,
    val attributes: MangaDxRelationshipAttributes? = null
)

@Serializable
data class MangaDxRelationshipAttributes(
    val name: String? = null,
    val fileName: String? = null
)

@Serializable
data class MangaDxPageResponse(
    val baseUrl: String,
    val chapter: MangaDxChapterData
)

@Serializable
data class MangaDxChapterData(
    val hash: String,
    val data: List<String>
)