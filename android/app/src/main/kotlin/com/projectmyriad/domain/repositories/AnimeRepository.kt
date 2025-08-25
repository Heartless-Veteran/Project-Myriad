package com.projectmyriad.domain.repositories

import com.projectmyriad.domain.entities.Anime
import com.projectmyriad.domain.entities.AnimeEpisode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for anime operations following Clean Architecture principles.
 */
interface AnimeRepository {
    
    /**
     * Get all anime in the local vault.
     */
    fun getAllAnime(): Flow<List<Anime>>
    
    /**
     * Get a specific anime by ID.
     */
    suspend fun getAnimeById(id: String): Result<Anime?>
    
    /**
     * Get anime episodes for a specific anime.
     */
    fun getEpisodesForAnime(animeId: String): Flow<List<AnimeEpisode>>
    
    /**
     * Import anime from local file (.mp4/.mkv/.avi).
     */
    suspend fun importAnimeFromFile(filePath: String): Result<Anime>
    
    /**
     * Delete anime and all associated data.
     */
    suspend fun deleteAnime(animeId: String): Result<Unit>
    
    /**
     * Update anime watching progress.
     */
    suspend fun updateWatchProgress(animeId: String, progress: Float): Result<Unit>
    
    /**
     * Update episode watching progress.
     */
    suspend fun updateEpisodeProgress(episodeId: String, progress: Float): Result<Unit>
    
    /**
     * Search anime by title, studio, or tags.
     */
    fun searchAnime(query: String): Flow<List<Anime>>
    
    /**
     * Get anime filtered by genres, status, etc.
     */
    fun getAnimeFiltered(
        genres: List<String> = emptyList(),
        status: List<String> = emptyList(),
        minRating: Float = 0f
    ): Flow<List<Anime>>
    
    /**
     * Mark anime as favorite/unfavorite.
     */
    suspend fun toggleFavorite(animeId: String): Result<Unit>
    
    /**
     * Get favorite anime.
     */
    fun getFavoriteAnime(): Flow<List<Anime>>
    
    /**
     * Get recently watched anime.
     */
    fun getRecentlyWatchedAnime(limit: Int = 10): Flow<List<Anime>>
    
    /**
     * Get continue watching list (anime with progress but not finished).
     */
    fun getContinueWatchingList(): Flow<List<Anime>>
}