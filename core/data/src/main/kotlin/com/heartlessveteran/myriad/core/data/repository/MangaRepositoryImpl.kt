package com.heartlessveteran.myriad.core.data.repository

import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.entities.MangaChapter
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.MangaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of MangaRepository interface.
 * Handles data operations for manga entities using local database.
 * Follows Clean Architecture by implementing domain repository interface.
 */
class MangaRepositoryImpl(
    // TODO: Inject actual DAO when database is properly configured
    // private val mangaDao: MangaDao,
    // private val chapterDao: ChapterDao
) : MangaRepository {

    // Temporary in-memory storage for demonstration
    private val libraryManga = mutableListOf<Manga>()
    private val chapters = mutableMapOf<String, List<MangaChapter>>()

    override fun getLibraryManga(): Flow<List<Manga>> = flow {
        emit(libraryManga.filter { it.isInLibrary })
    }

    override suspend fun getMangaById(id: String): Result<Manga> {
        return try {
            val manga = libraryManga.find { it.id == id }
            if (manga != null) {
                Result.Success(manga)
            } else {
                Result.Error(
                    NoSuchElementException("Manga not found"),
                    "Manga with ID '$id' not found in library"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get manga: ${e.message}")
        }
    }

    override suspend fun saveManga(manga: Manga): Result<Unit> {
        return try {
            val existingIndex = libraryManga.indexOfFirst { it.id == manga.id }
            if (existingIndex != -1) {
                libraryManga[existingIndex] = manga
            } else {
                libraryManga.add(manga)
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to save manga: ${e.message}")
        }
    }

    override suspend fun removeManga(mangaId: String): Result<Unit> {
        return try {
            val removed = libraryManga.removeIf { it.id == mangaId }
            if (removed) {
                chapters.remove(mangaId)
                Result.Success(Unit)
            } else {
                Result.Error(
                    NoSuchElementException("Manga not found"),
                    "Manga with ID '$mangaId' not found"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to remove manga: ${e.message}")
        }
    }

    override suspend fun updateReadingProgress(mangaId: String, readChapters: Int): Result<Unit> {
        return try {
            val mangaIndex = libraryManga.indexOfFirst { it.id == mangaId }
            if (mangaIndex != -1) {
                val manga = libraryManga[mangaIndex]
                libraryManga[mangaIndex] = manga.copy(
                    readChapters = readChapters,
                    lastReadDate = java.util.Date()
                )
                Result.Success(Unit)
            } else {
                Result.Error(
                    NoSuchElementException("Manga not found"),
                    "Manga with ID '$mangaId' not found"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to update reading progress: ${e.message}")
        }
    }

    override suspend fun toggleFavorite(mangaId: String): Result<Manga> {
        return try {
            val mangaIndex = libraryManga.indexOfFirst { it.id == mangaId }
            if (mangaIndex != -1) {
                val manga = libraryManga[mangaIndex]
                val updatedManga = manga.copy(isFavorite = !manga.isFavorite)
                libraryManga[mangaIndex] = updatedManga
                Result.Success(updatedManga)
            } else {
                Result.Error(
                    NoSuchElementException("Manga not found"),
                    "Manga with ID '$mangaId' not found"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to toggle favorite: ${e.message}")
        }
    }

    override fun searchLibraryManga(query: String): Flow<List<Manga>> = flow {
        val filtered = libraryManga.filter { manga ->
            manga.isInLibrary && (
                manga.title.contains(query, ignoreCase = true) ||
                manga.author.contains(query, ignoreCase = true) ||
                manga.genres.any { it.contains(query, ignoreCase = true) }
            )
        }
        emit(filtered)
    }

    override fun getMangaByGenre(genre: String): Flow<List<Manga>> = flow {
        val filtered = libraryManga.filter { manga ->
            manga.isInLibrary && manga.genres.any { it.equals(genre, ignoreCase = true) }
        }
        emit(filtered)
    }

    override fun getRecentlyReadManga(limit: Int): Flow<List<Manga>> = flow {
        val recentlyRead = libraryManga
            .filter { it.isInLibrary && it.lastReadDate != null }
            .sortedByDescending { it.lastReadDate }
            .take(limit)
        emit(recentlyRead)
    }

    override fun getChaptersForManga(mangaId: String): Flow<List<MangaChapter>> = flow {
        emit(chapters[mangaId] ?: emptyList())
    }

    override suspend fun saveChapter(chapter: MangaChapter): Result<Unit> {
        return try {
            val mangaChapters = chapters[chapter.mangaId]?.toMutableList() ?: mutableListOf()
            val existingIndex = mangaChapters.indexOfFirst { it.id == chapter.id }
            if (existingIndex != -1) {
                mangaChapters[existingIndex] = chapter
            } else {
                mangaChapters.add(chapter)
            }
            chapters[chapter.mangaId] = mangaChapters
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to save chapter: ${e.message}")
        }
    }

    override suspend fun updateChapterProgress(
        chapterId: String,
        isRead: Boolean,
        lastReadPage: Int
    ): Result<Unit> {
        return try {
            var updated = false
            chapters.forEach { (mangaId, chapterList) ->
                val chapterIndex = chapterList.indexOfFirst { it.id == chapterId }
                if (chapterIndex != -1) {
                    val chapter = chapterList[chapterIndex]
                    val updatedChapter = chapter.copy(
                        isRead = isRead,
                        lastReadPage = lastReadPage,
                        dateRead = if (isRead) java.util.Date() else null
                    )
                    chapters[mangaId] = chapterList.toMutableList().apply {
                        set(chapterIndex, updatedChapter)
                    }
                    updated = true
                    return@forEach
                }
            }
            if (updated) {
                Result.Success(Unit)
            } else {
                Result.Error(
                    NoSuchElementException("Chapter not found"),
                    "Chapter with ID '$chapterId' not found"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to update chapter progress: ${e.message}")
        }
    }
}