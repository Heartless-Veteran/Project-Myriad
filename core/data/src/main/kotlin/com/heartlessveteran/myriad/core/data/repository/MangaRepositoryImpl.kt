package com.heartlessveteran.myriad.core.data.repository

import com.heartlessveteran.myriad.core.data.database.ChapterDao
import com.heartlessveteran.myriad.core.data.database.MangaDao
import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.entities.MangaChapter
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.MangaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.*

/**
 * Implementation of MangaRepository interface.
 * Handles data operations for manga entities using Room database.
 * Follows Clean Architecture by implementing domain repository interface.
 */
class MangaRepositoryImpl(
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao
) : MangaRepository {

    override fun getLibraryManga(): Flow<List<Manga>> = mangaDao.getLibraryManga()

    override suspend fun getMangaById(id: String): Result<Manga> {
        return try {
            val manga = mangaDao.getMangaById(id)
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
            mangaDao.insertManga(manga)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to save manga: ${e.message}")
        }
    }

    override suspend fun removeManga(mangaId: String): Result<Unit> {
        return try {
            mangaDao.deleteMangaById(mangaId)
            chapterDao.deleteChaptersForManga(mangaId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to remove manga: ${e.message}")
        }
    }

    override suspend fun updateReadingProgress(mangaId: String, readChapters: Int): Result<Unit> {
        return try {
            val currentDate = Date().time
            mangaDao.updateReadingProgress(mangaId, readChapters, currentDate)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update reading progress: ${e.message}")
        }
    }

    override suspend fun toggleFavorite(mangaId: String): Result<Manga> {
        return try {
            val manga = mangaDao.getMangaById(mangaId)
            if (manga != null) {
                val updatedManga = manga.copy(isFavorite = !manga.isFavorite)
                mangaDao.updateManga(updatedManga)
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

    override fun searchLibraryManga(query: String): Flow<List<Manga>> = 
        mangaDao.searchLibraryManga(query)

    override fun getMangaByGenre(genre: String): Flow<List<Manga>> = 
        mangaDao.getMangaByGenre(genre)

    override fun getRecentlyReadManga(limit: Int): Flow<List<Manga>> = 
        mangaDao.getRecentlyReadManga(limit)

    override fun getChaptersForManga(mangaId: String): Flow<List<MangaChapter>> = 
        chapterDao.getChaptersForManga(mangaId)

    override suspend fun saveChapter(chapter: MangaChapter): Result<Unit> {
        return try {
            chapterDao.insertChapter(chapter)
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
            val dateRead = if (isRead) Date().time else null
            chapterDao.updateChapterProgress(chapterId, isRead, lastReadPage, dateRead)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update chapter progress: ${e.message}")
        }
    }

    override suspend fun getChaptersForMangaList(mangaId: String): List<MangaChapter> {
        return chapterDao.getChaptersForManga(mangaId).first()
    }

    override suspend fun getChapterById(chapterId: String): MangaChapter? {
        return try {
            chapterDao.getChapterById(chapterId)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateManga(manga: Manga): Result<Unit> {
        return try {
            mangaDao.updateManga(manga)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update manga: ${e.message}")
        }
    }

    override suspend fun updateChapter(chapter: MangaChapter): Result<Unit> {
        return try {
            chapterDao.updateChapter(chapter)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update chapter: ${e.message}")
        }
    }
}