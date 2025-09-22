package com.heartlessveteran.myriad.feature.vault.domain.usecase

import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.entities.MangaChapter
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.MangaRepository
import java.util.Date

/**
 * Use case for updating reading progress.
 * Handles the business logic for tracking user reading progress.
 */
class UpdateReadingProgressUseCase(
    private val mangaRepository: MangaRepository
) {
    /**
     * Updates reading progress for a specific chapter and manga
     * @param mangaId ID of the manga being read
     * @param chapterId ID of the chapter being read
     * @param currentPage Current page number (0-indexed)
     * @param totalPages Total number of pages in the chapter
     * @param isCompleted Whether the chapter was completed
     */
    suspend operator fun invoke(
        mangaId: String,
        chapterId: String,
        currentPage: Int,
        totalPages: Int,
        isCompleted: Boolean = false
    ): Result<Unit> {
        return try {
            // Update chapter progress
            val chapter = mangaRepository.getChapterById(chapterId)
            if (chapter != null) {
                val updatedChapter = chapter.copy(
                    lastReadPage = currentPage,
                    isRead = isCompleted || (currentPage >= totalPages - 1),
                    dateRead = if (isCompleted) Date() else chapter.dateRead
                )
                mangaRepository.updateChapter(updatedChapter)
            }

            // Update manga progress
            val manga = mangaRepository.getMangaById(mangaId)
            if (manga != null) {
                val allChapters = mangaRepository.getChaptersForManga(mangaId)
                val readChapters = allChapters.count { it.isRead }
                
                val updatedManga = manga.copy(
                    readChapters = readChapters,
                    lastReadDate = Date()
                )
                mangaRepository.updateManga(updatedManga)
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update reading progress: ${e.message}")
        }
    }
}

/**
 * Use case for getting reading progress.
 * Retrieves current reading progress for a manga/chapter.
 */
class GetReadingProgressUseCase(
    private val mangaRepository: MangaRepository
) {
    /**
     * Gets reading progress for a specific chapter
     * @param chapterId ID of the chapter
     * @return Result containing reading progress data
     */
    suspend operator fun invoke(chapterId: String): Result<ReadingProgress> {
        return try {
            val chapter = mangaRepository.getChapterById(chapterId)
            if (chapter != null) {
                val progress = ReadingProgress(
                    chapterId = chapter.id,
                    currentPage = chapter.lastReadPage,
                    totalPages = chapter.pages.size,
                    isCompleted = chapter.isRead,
                    progressPercentage = if (chapter.pages.isNotEmpty()) {
                        (chapter.lastReadPage + 1).toFloat() / chapter.pages.size.toFloat()
                    } else 0f
                )
                Result.Success(progress)
            } else {
                Result.Error(
                    IllegalArgumentException("Chapter not found"),
                    "Chapter with ID $chapterId not found"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get reading progress: ${e.message}")
        }
    }
}

/**
 * Data class representing reading progress
 */
data class ReadingProgress(
    val chapterId: String,
    val currentPage: Int,
    val totalPages: Int,
    val isCompleted: Boolean,
    val progressPercentage: Float
)