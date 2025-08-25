package com.projectmyriad.domain.repositories

import com.projectmyriad.domain.entities.Manga
import com.projectmyriad.domain.entities.MangaChapter
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for manga operations following Clean Architecture principles.
 * This interface defines the contract that data layer implementations must follow.
 */
interface MangaRepository {
    
    /**
     * Get all manga in the local vault.
     */
    fun getAllManga(): Flow<List<Manga>>
    
    /**
     * Get a specific manga by ID.
     */
    suspend fun getMangaById(id: String): Result<Manga?>
    
    /**
     * Get manga chapters for a specific manga.
     */
    fun getChaptersForManga(mangaId: String): Flow<List<MangaChapter>>
    
    /**
     * Import manga from local file (.cbz/.cbr).
     */
    suspend fun importMangaFromFile(filePath: String): Result<Manga>
    
    /**
     * Delete manga and all associated data.
     */
    suspend fun deleteManga(mangaId: String): Result<Unit>
    
    /**
     * Update manga reading progress.
     */
    suspend fun updateReadingProgress(mangaId: String, progress: Float): Result<Unit>
    
    /**
     * Update chapter reading progress.
     */
    suspend fun updateChapterProgress(chapterId: String, progress: Float): Result<Unit>
    
    /**
     * Search manga by title, author, or tags.
     */
    fun searchManga(query: String): Flow<List<Manga>>
    
    /**
     * Get manga filtered by genres, status, etc.
     */
    fun getMangaFiltered(
        genres: List<String> = emptyList(),
        status: List<String> = emptyList(),
        minRating: Float = 0f
    ): Flow<List<Manga>>
    
    /**
     * Mark manga as favorite/unfavorite.
     */
    suspend fun toggleFavorite(mangaId: String): Result<Unit>
    
    /**
     * Get favorite manga.
     */
    fun getFavoriteManga(): Flow<List<Manga>>
    
    /**
     * Get recently read manga.
     */
    fun getRecentlyReadManga(limit: Int = 10): Flow<List<Manga>>
}