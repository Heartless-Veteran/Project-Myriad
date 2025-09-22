package com.heartlessveteran.myriad.core.domain.repository

import com.heartlessveteran.myriad.core.domain.entities.Anime
import com.heartlessveteran.myriad.core.domain.entities.AnimeEpisode
import com.heartlessveteran.myriad.core.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for anime data operations.
 * Handles both local database and remote API interactions.
 * Follows Clean Architecture principles by residing in the domain layer.
 */
interface AnimeRepository {
    /**
     * Observes all anime in the user's library
     *
     * @return Flow of anime list from local database
     */
    fun getLibraryAnime(): Flow<List<Anime>>

    /**
     * Gets a specific anime by ID
     *
     * @param id Anime identifier
     * @return Result containing anime or error
     */
    suspend fun getAnimeById(id: String): Result<Anime>

    /**
     * Inserts or updates an anime in the local database
     *
     * @param anime Anime to save
     * @return Result indicating success or error
     */
    suspend fun saveAnime(anime: Anime): Result<Unit>

    /**
     * Removes an anime from the library
     *
     * @param animeId Anime identifier to remove
     * @return Result indicating success or error
     */
    suspend fun removeAnime(animeId: String): Result<Unit>

    /**
     * Updates anime watching progress
     *
     * @param animeId Anime identifier
     * @param watchedEpisodes Number of episodes watched
     * @return Result indicating success or error
     */
    suspend fun updateWatchingProgress(animeId: String, watchedEpisodes: Int): Result<Unit>

    /**
     * Toggles favorite status of an anime
     *
     * @param animeId Anime identifier
     * @return Result containing updated anime or error
     */
    suspend fun toggleFavorite(animeId: String): Result<Anime>

    /**
     * Searches anime in the library
     *
     * @param query Search query
     * @return Flow of matching anime
     */
    fun searchLibraryAnime(query: String): Flow<List<Anime>>

    /**
     * Gets anime by genre
     *
     * @param genre Genre to filter by
     * @return Flow of anime with the specified genre
     */
    fun getAnimeByGenre(genre: String): Flow<List<Anime>>

    /**
     * Gets recently watched anime
     *
     * @param limit Maximum number of anime to return
     * @return Flow of recently watched anime
     */
    fun getRecentlyWatchedAnime(limit: Int = 10): Flow<List<Anime>>

    /**
     * Gets episodes for a specific anime
     *
     * @param animeId Anime identifier
     * @return Flow of episodes for the anime
     */
    fun getEpisodesForAnime(animeId: String): Flow<List<AnimeEpisode>>

    /**
     * Saves an episode to the database
     *
     * @param episode Episode to save
     * @return Result indicating success or error
     */
    suspend fun saveEpisode(episode: AnimeEpisode): Result<Unit>

    /**
     * Updates episode watching progress
     *
     * @param episodeId Episode identifier
     * @param isWatched Whether the episode has been watched
     * @param watchProgress Watch progress in milliseconds
     * @return Result indicating success or error
     */
    suspend fun updateEpisodeProgress(
        episodeId: String,
        isWatched: Boolean,
        watchProgress: Long = 0L
    ): Result<Unit>

    /**
     * Gets next unwatched episode for an anime
     *
     * @param animeId Anime identifier
     * @return Result containing next episode or error
     */
    suspend fun getNextUnwatchedEpisode(animeId: String): Result<AnimeEpisode?>

    /**
     * Imports anime from local file path
     *
     * @param filePath Path to anime file or directory
     * @return Result containing imported anime or error
     */
    suspend fun importAnimeFromFile(filePath: String): Result<Anime>
}