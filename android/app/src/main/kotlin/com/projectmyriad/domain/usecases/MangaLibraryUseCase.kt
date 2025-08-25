package com.projectmyriad.domain.usecases

import com.projectmyriad.domain.entities.Manga
import com.projectmyriad.domain.repositories.MangaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for managing manga library operations.
 * Encapsulates business logic for manga management.
 */
@Singleton
class MangaLibraryUseCase @Inject constructor(
    private val mangaRepository: MangaRepository
) {
    
    /**
     * Get all manga from the vault.
     */
    fun getAllManga(): Flow<List<Manga>> {
        return mangaRepository.getAllManga()
    }
    
    /**
     * Import manga from a file path.
     */
    suspend fun importManga(filePath: String): Result<Manga> {
        return try {
            mangaRepository.importMangaFromFile(filePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete manga from the vault.
     */
    suspend fun deleteManga(mangaId: String): Result<Unit> {
        return mangaRepository.deleteManga(mangaId)
    }
    
    /**
     * Update reading progress for a manga.
     */
    suspend fun updateReadingProgress(mangaId: String, progress: Float): Result<Unit> {
        val clampedProgress = progress.coerceIn(0f, 1f)
        return mangaRepository.updateReadingProgress(mangaId, clampedProgress)
    }
    
    /**
     * Search manga with query string.
     */
    fun searchManga(query: String): Flow<List<Manga>> {
        return if (query.isBlank()) {
            mangaRepository.getAllManga()
        } else {
            mangaRepository.searchManga(query.trim())
        }
    }
    
    /**
     * Get filtered manga based on criteria.
     */
    fun getFilteredManga(
        genres: List<String> = emptyList(),
        status: List<String> = emptyList(),
        minRating: Float = 0f
    ): Flow<List<Manga>> {
        return mangaRepository.getMangaFiltered(genres, status, minRating)
    }
    
    /**
     * Toggle favorite status of a manga.
     */
    suspend fun toggleFavorite(mangaId: String): Result<Unit> {
        return mangaRepository.toggleFavorite(mangaId)
    }
    
    /**
     * Get recently read manga.
     */
    fun getRecentlyRead(limit: Int = 10): Flow<List<Manga>> {
        return mangaRepository.getRecentlyReadManga(limit)
    }
}