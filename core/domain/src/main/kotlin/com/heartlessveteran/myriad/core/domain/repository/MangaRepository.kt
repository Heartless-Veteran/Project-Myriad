package com.heartlessveteran.myriad.core.domain.repository

import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.entities.MangaChapter
import com.heartlessveteran.myriad.core.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for manga data operations.
 * Handles both local database and remote API interactions.
 * Follows Clean Architecture principles by residing in the domain layer.
 */
interface MangaRepository {
    /**
     * Observes all manga in the user's library
     *
     * @return Flow of manga list from local database
     */
    fun getLibraryManga(): Flow<List<Manga>>

    /**
     * Gets a specific manga by ID
     *
     * @param id Manga identifier
     * @return Result containing manga or error
     */
    suspend fun getMangaById(id: String): Result<Manga>

    /**
     * Inserts or updates a manga in the local database
     *
     * @param manga Manga to save
     * @return Result indicating success or error
     */
    suspend fun saveManga(manga: Manga): Result<Unit>

    /**
     * Removes a manga from the library
     *
     * @param mangaId Manga identifier to remove
     * @return Result indicating success or error
     */
    suspend fun removeManga(mangaId: String): Result<Unit>

    /**
     * Updates manga reading progress
     *
     * @param mangaId Manga identifier
     * @param readChapters Number of chapters read
     * @return Result indicating success or error
     */
    suspend fun updateReadingProgress(mangaId: String, readChapters: Int): Result<Unit>

    /**
     * Toggles favorite status of a manga
     *
     * @param mangaId Manga identifier
     * @return Result containing updated manga or error
     */
    suspend fun toggleFavorite(mangaId: String): Result<Manga>

    /**
     * Searches manga in the library
     *
     * @param query Search query
     * @return Flow of matching manga
     */
    fun searchLibraryManga(query: String): Flow<List<Manga>>

    /**
     * Gets manga by genre
     *
     * @param genre Genre to filter by
     * @return Flow of manga with the specified genre
     */
    fun getMangaByGenre(genre: String): Flow<List<Manga>>

    /**
     * Gets recently read manga
     *
     * @param limit Maximum number of manga to return
     * @return Flow of recently read manga
     */
    fun getRecentlyReadManga(limit: Int = 10): Flow<List<Manga>>

    /**
     * Gets chapters for a specific manga
     *
     * @param mangaId Manga identifier
     * @return Flow of chapters for the manga
     */
    fun getChaptersForManga(mangaId: String): Flow<List<MangaChapter>>

    /**
     * Saves a chapter to the database
     *
     * @param chapter Chapter to save
     * @return Result indicating success or error
     */
    suspend fun saveChapter(chapter: MangaChapter): Result<Unit>

    /**
     * Updates chapter reading progress
     *
     * @param chapterId Chapter identifier
     * @param isRead Whether the chapter has been read
     * @param lastReadPage Last page read in the chapter
     * @return Result indicating success or error
     */
    suspend fun updateChapterProgress(
        chapterId: String,
        isRead: Boolean,
        lastReadPage: Int = 0
    ): Result<Unit>
}