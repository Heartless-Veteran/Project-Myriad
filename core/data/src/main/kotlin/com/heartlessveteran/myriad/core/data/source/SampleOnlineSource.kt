package com.heartlessveteran.myriad.core.data.source

import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.entities.MangaChapter
import com.heartlessveteran.myriad.core.domain.entities.MangaStatus
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.Source
import kotlinx.coroutines.delay
import java.util.Date

/**
 * Sample online manga source implementation (MangaDex-like)
 * This demonstrates how an online source plugin would work
 * In a real implementation, this would make actual HTTP requests
 */
class SampleOnlineSource : Source {
    override val id: String = "sample_online"
    override val name: String = "Sample Online"
    override val lang: String = "en"
    override val baseUrl: String = "https://api.sample-manga.com"

    // Sample online manga data for demonstration
    private val sampleOnlineManga = listOf(
        Manga(
            id = "online-manga-1",
            title = "Demon Slayer",
            description = "A young boy fights demons to save his sister.",
            author = "Koyoharu Gotouge",
            artist = "Koyoharu Gotouge",
            status = MangaStatus.COMPLETED,
            genres = listOf("Action", "Supernatural", "Historical"),
            totalChapters = 205,
            rating = 9.2f,
            isLocal = false,
            source = "sample_online",
            sourceId = "demon-slayer-kimetsu-no-yaiba",
            coverImageUrl = "https://sample-manga.com/covers/demon-slayer.jpg"
        ),
        Manga(
            id = "online-manga-2",
            title = "One Piece",
            description = "A pirate adventure in search of the ultimate treasure.",
            author = "Eiichiro Oda",
            artist = "Eiichiro Oda",
            status = MangaStatus.ONGOING,
            genres = listOf("Action", "Adventure", "Comedy"),
            totalChapters = 1000,
            rating = 9.5f,
            isLocal = false,
            source = "sample_online",
            sourceId = "one-piece",
            coverImageUrl = "https://sample-manga.com/covers/one-piece.jpg"
        ),
        Manga(
            id = "online-manga-3",
            title = "Attack on Titan",
            description = "Humanity fights for survival against giant titans.",
            author = "Hajime Isayama",
            artist = "Hajime Isayama",
            status = MangaStatus.COMPLETED,
            genres = listOf("Action", "Drama", "Horror"),
            totalChapters = 139,
            rating = 9.0f,
            isLocal = false,
            source = "sample_online",
            sourceId = "attack-on-titan",
            coverImageUrl = "https://sample-manga.com/covers/aot.jpg"
        )
    )

    override suspend fun getLatestManga(page: Int): Result<List<Manga>> {
        return try {
            // Simulate network delay
            delay(500)
            
            val sortedByDate = sampleOnlineManga.sortedByDescending { it.lastUpdated }
            val startIndex = (page - 1) * 20
            val endIndex = minOf(startIndex + 20, sortedByDate.size)
            
            if (startIndex >= sortedByDate.size) {
                Result.Success(emptyList())
            } else {
                val mangaPage = sortedByDate.subList(startIndex, endIndex)
                Result.Success(mangaPage)
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get latest manga from $name: ${e.message}")
        }
    }

    override suspend fun getMangaDetails(url: String): Result<Manga> {
        return try {
            // Simulate network delay
            delay(300)
            
            val manga = sampleOnlineManga.find { it.sourceId == url || it.id == url }
            if (manga != null) {
                Result.Success(manga)
            } else {
                Result.Error(
                    NoSuchElementException("Manga not found"),
                    "No manga found with identifier: $url on $name"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get manga details from $name: ${e.message}")
        }
    }

    override suspend fun getChapterPages(url: String): Result<List<String>> {
        return try {
            // Simulate network delay
            delay(400)
            
            // In a real implementation, this would fetch page URLs from the API
            val pages = generateSampleOnlinePages(url)
            Result.Success(pages)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get chapter pages from $name: ${e.message}")
        }
    }

    override suspend fun searchManga(query: String, page: Int): Result<List<Manga>> {
        return try {
            // Simulate network delay
            delay(600)
            
            val filtered = sampleOnlineManga.filter { manga ->
                manga.title.contains(query, ignoreCase = true) ||
                manga.author.contains(query, ignoreCase = true) ||
                manga.genres.any { it.contains(query, ignoreCase = true) }
            }
            
            val startIndex = (page - 1) * 20
            val endIndex = minOf(startIndex + 20, filtered.size)
            
            if (startIndex >= filtered.size) {
                Result.Success(emptyList())
            } else {
                val mangaPage = filtered.subList(startIndex, endIndex)
                Result.Success(mangaPage)
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to search manga on $name: ${e.message}")
        }
    }

    override suspend fun getPopularManga(page: Int): Result<List<Manga>> {
        return try {
            // Simulate network delay
            delay(500)
            
            val sortedByRating = sampleOnlineManga.sortedByDescending { it.rating }
            
            val startIndex = (page - 1) * 20
            val endIndex = minOf(startIndex + 20, sortedByRating.size)
            
            if (startIndex >= sortedByRating.size) {
                Result.Success(emptyList())
            } else {
                val mangaPage = sortedByRating.subList(startIndex, endIndex)
                Result.Success(mangaPage)
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get popular manga from $name: ${e.message}")
        }
    }

    override suspend fun getChapterList(manga: Manga): Result<List<MangaChapter>> {
        return try {
            // Simulate network delay
            delay(400)
            
            val chapters = generateSampleOnlineChapters(manga)
            Result.Success(chapters)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get chapter list from $name: ${e.message}")
        }
    }

    /**
     * Generates sample online page URLs for a chapter
     */
    private fun generateSampleOnlinePages(chapterUrl: String): List<String> {
        return (1..15).map { pageNum ->
            "$baseUrl/chapters/$chapterUrl/pages/${pageNum.toString().padStart(3, '0')}.jpg"
        }
    }

    /**
     * Generates sample online chapters for a manga
     */
    private fun generateSampleOnlineChapters(manga: Manga): List<MangaChapter> {
        return (1..minOf(manga.totalChapters, 10)).map { chapterNum ->
            MangaChapter(
                id = "${manga.id}_chapter_$chapterNum",
                mangaId = manga.id,
                chapterNumber = chapterNum.toFloat(),
                title = "Chapter $chapterNum",
                isDownloaded = false,
                localPath = null,
                dateAdded = Date(),
                source = "sample_online",
                sourceChapterId = "${manga.sourceId}/chapter-$chapterNum"
            )
        }
    }
}