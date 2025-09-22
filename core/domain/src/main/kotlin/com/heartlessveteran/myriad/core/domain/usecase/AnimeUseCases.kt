package com.heartlessveteran.myriad.core.domain.usecase

import com.heartlessveteran.myriad.core.domain.entities.Anime
import com.heartlessveteran.myriad.core.domain.entities.AnimeEpisode
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for getting library anime.
 * Handles business logic for retrieving user's anime library.
 * Returns reactive Flow for automatic UI updates when data changes.
 */
@Singleton
class GetLibraryAnimeUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    /**
     * Gets all anime in the user's library as a reactive stream
     *
     * @return Flow of anime list that updates automatically
     */
    operator fun invoke(): Flow<List<Anime>> {
        return animeRepository.getLibraryAnime()
    }
}

/**
 * Use case for getting anime details by ID.
 * Handles business logic for retrieving specific anime information.
 */
@Singleton
class GetAnimeDetailsUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    /**
     * Gets anime details by ID
     *
     * @param animeId The anime identifier
     * @return Result containing anime details or error
     */
    suspend operator fun invoke(animeId: String): Result<Anime> {
        return if (animeId.isBlank()) {
            Result.Error(
                IllegalArgumentException("Anime ID cannot be blank"),
                "Invalid anime ID provided"
            )
        } else {
            animeRepository.getAnimeById(animeId)
        }
    }
}

/**
 * Use case for adding anime to library.
 * Handles business logic for saving anime to the local database.
 */
@Singleton
class AddAnimeToLibraryUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    /**
     * Adds anime to the user's library
     *
     * @param anime The anime to add
     * @return Result indicating success or error
     */
    suspend operator fun invoke(anime: Anime): Result<Unit> {
        return try {
            // Ensure anime is marked as in library
            val libraryAnime = anime.copy(
                isInLibrary = true,
                dateAdded = java.util.Date()
            )
            animeRepository.saveAnime(libraryAnime)
        } catch (e: Exception) {
            Result.Error(e, "Failed to add anime to library: ${e.message}")
        }
    }
}

/**
 * Use case for getting anime episodes.
 * Handles business logic for retrieving episodes for a specific anime.
 */
@Singleton
class GetAnimeEpisodesUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    /**
     * Gets episodes for a specific anime
     *
     * @param animeId The anime identifier
     * @return Flow of episodes for the anime
     */
    operator fun invoke(animeId: String): Flow<List<AnimeEpisode>> {
        return animeRepository.getEpisodesForAnime(animeId)
    }
}

/**
 * Use case for updating episode watch progress.
 * Handles business logic for tracking viewing progress.
 */
@Singleton
class UpdateEpisodeProgressUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    /**
     * Updates episode watch progress
     *
     * @param episodeId Episode identifier
     * @param isWatched Whether the episode is marked as watched
     * @param watchProgress Progress in milliseconds
     * @return Result indicating success or error
     */
    suspend operator fun invoke(
        episodeId: String,
        isWatched: Boolean,
        watchProgress: Long = 0L
    ): Result<Unit> {
        return if (episodeId.isBlank()) {
            Result.Error(
                IllegalArgumentException("Episode ID cannot be blank"),
                "Invalid episode ID provided"
            )
        } else {
            animeRepository.updateEpisodeProgress(episodeId, isWatched, watchProgress)
        }
    }
}

/**
 * Use case for getting next unwatched episode.
 * Handles business logic for binge-watching functionality.
 */
@Singleton
class GetNextUnwatchedEpisodeUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    /**
     * Gets the next unwatched episode for an anime
     *
     * @param animeId The anime identifier
     * @return Result containing next episode or null if all watched
     */
    suspend operator fun invoke(animeId: String): Result<AnimeEpisode?> {
        return if (animeId.isBlank()) {
            Result.Error(
                IllegalArgumentException("Anime ID cannot be blank"),
                "Invalid anime ID provided"
            )
        } else {
            animeRepository.getNextUnwatchedEpisode(animeId)
        }
    }
}

/**
 * Use case for importing anime from file.
 * Handles business logic for local file import and metadata extraction.
 */
@Singleton
class ImportAnimeFromFileUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    /**
     * Imports anime from a local file or directory
     *
     * @param filePath Path to the anime file or directory
     * @return Result containing imported anime or error
     */
    suspend operator fun invoke(filePath: String): Result<Anime> {
        return if (filePath.isBlank()) {
            Result.Error(
                IllegalArgumentException("File path cannot be blank"),
                "Invalid file path provided"
            )
        } else {
            // Validate file extension
            val supportedExtensions = listOf(".mp4", ".mkv", ".avi")
            val isValidFile = supportedExtensions.any { ext ->
                filePath.lowercase().endsWith(ext)
            }
            
            if (!isValidFile && !java.io.File(filePath).isDirectory) {
                Result.Error(
                    IllegalArgumentException("Unsupported file format"),
                    "Supported formats: .mp4, .mkv, .avi"
                )
            } else {
                animeRepository.importAnimeFromFile(filePath)
            }
        }
    }
}

/**
 * Use case for searching library anime.
 * Handles business logic for anime search functionality.
 */
@Singleton
class SearchLibraryAnimeUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    /**
     * Searches anime in the library
     *
     * @param query Search query
     * @return Flow of matching anime
     */
    operator fun invoke(query: String): Flow<List<Anime>> {
        return animeRepository.searchLibraryAnime(query.trim())
    }
}