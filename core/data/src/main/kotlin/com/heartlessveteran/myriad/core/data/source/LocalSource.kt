package com.heartlessveteran.myriad.core.data.source

import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.entities.MangaChapter
import com.heartlessveteran.myriad.core.domain.entities.MangaStatus
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.Source
import java.util.Date

/**
 * Local source implementation for reading manga from local storage.
 * Demonstrates the Source interface implementation for local files.
 * Supports .cbz, .cbr, and directory-based manga storage.
 */
class LocalSource : Source {
    override val id: String = "local"
    override val name: String = "Local Storage"
    override val lang: String = "en"
    override val baseUrl: String = ""

    // Sample local manga data for demonstration
    private val sampleManga = listOf(
        Manga(
            id = "sample-manga-1",
            title = "Sample Manga Title",
            description = "A sample manga stored locally for testing purposes.",
            author = "Sample Author",
            artist = "Sample Artist",
            status = MangaStatus.ONGOING,
            genres = listOf("Action", "Adventure"),
            totalChapters = 10,
            isLocal = true,
            source = "local",
            localPath = "/storage/manga/sample-manga/"
        ),
        Manga(
            id = "sample-manga-2",
            title = "Another Local Manga",
            description = "Another sample manga for testing the local source.",
            author = "Test Author",
            status = MangaStatus.COMPLETED,
            genres = listOf("Romance", "Comedy"),
            totalChapters = 5,
            isLocal = true,
            source = "local",
            localPath = "/storage/manga/another-manga/"
        )
    )

    override suspend fun getLatestManga(page: Int): Result<List<Manga>> {
        return try {
            // In a real implementation, this would scan the local directory
            // and return manga sorted by last modified date
            val startIndex = (page - 1) * 20
            val endIndex = minOf(startIndex + 20, sampleManga.size)
            
            if (startIndex >= sampleManga.size) {
                Result.Success(emptyList())
            } else {
                val mangaPage = sampleManga.subList(startIndex, endIndex)
                Result.Success(mangaPage)
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get local manga: ${e.message}")
        }
    }

    override suspend fun getMangaDetails(url: String): Result<Manga> {
        return try {
            // For local source, URL would be the local path or manga ID
            val manga = sampleManga.find { it.id == url || it.localPath == url }
            if (manga != null) {
                Result.Success(manga)
            } else {
                Result.Error(
                    NoSuchElementException("Manga not found"),
                    "No local manga found with identifier: $url"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get manga details: ${e.message}")
        }
    }

    override suspend fun getChapterPages(url: String): Result<List<String>> {
        return try {
            // For local source, this would read pages from the chapter directory
            // or extract from .cbz/.cbr archives
            val pages = generateSamplePages(url)
            Result.Success(pages)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get chapter pages: ${e.message}")
        }
    }

    override suspend fun searchManga(query: String, page: Int): Result<List<Manga>> {
        return try {
            val filtered = sampleManga.filter { manga ->
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
            Result.Error(e, "Failed to search local manga: ${e.message}")
        }
    }

    override suspend fun getPopularManga(page: Int): Result<List<Manga>> {
        return try {
            // For local source, "popular" could be based on read count or rating
            val sortedByRating = sampleManga.sortedByDescending { it.rating }
            
            val startIndex = (page - 1) * 20
            val endIndex = minOf(startIndex + 20, sortedByRating.size)
            
            if (startIndex >= sortedByRating.size) {
                Result.Success(emptyList())
            } else {
                val mangaPage = sortedByRating.subList(startIndex, endIndex)
                Result.Success(mangaPage)
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get popular local manga: ${e.message}")
        }
    }

    override suspend fun getChapterList(manga: Manga): Result<List<MangaChapter>> {
        return try {
            // Generate sample chapters for the manga
            val chapters = generateSampleChapters(manga)
            Result.Success(chapters)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get chapter list: ${e.message}")
        }
    }

    /**
     * Generates sample pages for a chapter (for demonstration)
     * In a real implementation, this would scan the chapter directory
     */
    private fun generateSamplePages(chapterUrl: String): List<String> {
        return (1..20).map { pageNum ->
            "$chapterUrl/page_${pageNum.toString().padStart(3, '0')}.jpg"
        }
    }

    /**
     * Generates sample chapters for a manga (for demonstration)
     * In a real implementation, this would scan the manga directory
     */
    private fun generateSampleChapters(manga: Manga): List<MangaChapter> {
        return (1..manga.totalChapters).map { chapterNum ->
            MangaChapter(
                id = "${manga.id}_chapter_$chapterNum",
                mangaId = manga.id,
                chapterNumber = chapterNum.toFloat(),
                title = "Chapter $chapterNum",
                isDownloaded = true,
                localPath = "${manga.localPath}/chapter_$chapterNum/",
                dateAdded = Date(),
                source = "local"
            )
        }
    }
}