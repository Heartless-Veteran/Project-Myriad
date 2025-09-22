package com.heartlessveteran.myriad.core.data.source

import com.heartlessveteran.myriad.core.domain.entities.Anime
import com.heartlessveteran.myriad.core.domain.entities.AnimeEpisode
import com.heartlessveteran.myriad.core.domain.entities.AnimeStatus
import com.heartlessveteran.myriad.core.domain.entities.AnimeType
import com.heartlessveteran.myriad.core.domain.entities.AnimeSeason
import com.heartlessveteran.myriad.core.domain.model.Result
import java.io.File
import java.util.Date

/**
 * Local anime source implementation for reading anime from local storage.
 * Handles .mp4, .mkv, and .avi files from local file system.
 * Supports both individual files and directory-based anime storage.
 */
class LocalAnimeSource {
    
    // Sample local anime data for demonstration
    private val sampleAnime = listOf(
        Anime(
            id = "sample-anime-1",
            title = "Sample Anime Series",
            alternativeTitles = listOf("Sample Anime", "サンプルアニメ"),
            description = "A sample anime series stored locally for testing purposes. This is a longer description to demonstrate the display of anime information.",
            studio = "Sample Studio",
            status = AnimeStatus.COMPLETED,
            genres = listOf("Action", "Adventure", "Supernatural"),
            tags = listOf("Magic", "Fantasy", "School"),
            rating = 8.5f,
            totalEpisodes = 24,
            watchedEpisodes = 12,
            isInLibrary = true,
            isFavorite = false,
            releaseYear = 2023,
            season = AnimeSeason.SPRING,
            type = AnimeType.TV,
            source = "local",
            isLocal = true,
            localPath = "/storage/anime/sample-anime/",
            duration = 24
        ),
        Anime(
            id = "sample-anime-2",
            title = "Another Sample Anime",
            alternativeTitles = listOf("Another Anime", "別のアニメ"),
            description = "Another sample anime for testing the local anime source functionality.",
            studio = "Another Studio",
            status = AnimeStatus.AIRING,
            genres = listOf("Romance", "Comedy", "Slice of Life"),
            tags = listOf("School", "Romance"),
            rating = 7.8f,
            totalEpisodes = 12,
            watchedEpisodes = 8,
            isInLibrary = true,
            isFavorite = true,
            releaseYear = 2024,
            season = AnimeSeason.WINTER,
            type = AnimeType.TV,
            source = "local",
            isLocal = true,
            localPath = "/storage/anime/another-anime/",
            duration = 23
        ),
        Anime(
            id = "sample-movie-1",
            title = "Sample Anime Movie",
            description = "A sample anime movie for testing movie format support.",
            studio = "Movie Studio",
            status = AnimeStatus.COMPLETED,
            genres = listOf("Action", "Drama"),
            rating = 9.2f,
            totalEpisodes = 1,
            watchedEpisodes = 0,
            isInLibrary = true,
            isFavorite = false,
            releaseYear = 2023,
            type = AnimeType.MOVIE,
            source = "local",
            isLocal = true,
            localPath = "/storage/anime/movies/sample-movie.mkv",
            duration = 120
        )
    )

    /**
     * Gets all anime from local storage
     */
    suspend fun getLocalAnime(): Result<List<Anime>> {
        return try {
            // In a real implementation, this would scan the local file system
            // for .mp4, .mkv, .avi files and directories
            Result.Success(sampleAnime)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get local anime: ${e.message}")
        }
    }

    /**
     * Gets a specific anime by ID
     */
    suspend fun getAnimeById(id: String): Result<Anime> {
        return try {
            val anime = sampleAnime.find { it.id == id }
            if (anime != null) {
                Result.Success(anime)
            } else {
                Result.Error(
                    IllegalArgumentException("Anime not found"),
                    "No anime found with ID: $id"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get anime details: ${e.message}")
        }
    }

    /**
     * Gets episodes for a specific anime
     */
    suspend fun getEpisodesForAnime(animeId: String): Result<List<AnimeEpisode>> {
        return try {
            val anime = sampleAnime.find { it.id == animeId }
            if (anime != null) {
                val episodes = generateSampleEpisodes(anime)
                Result.Success(episodes)
            } else {
                Result.Error(
                    IllegalArgumentException("Anime not found"),
                    "No anime found with ID: $animeId"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get episodes: ${e.message}")
        }
    }

    /**
     * Searches anime in the local collection
     */
    suspend fun searchAnime(query: String): Result<List<Anime>> {
        return try {
            val filtered = sampleAnime.filter { anime ->
                anime.title.contains(query, ignoreCase = true) ||
                anime.alternativeTitles.any { it.contains(query, ignoreCase = true) } ||
                anime.description.contains(query, ignoreCase = true) ||
                anime.studio.contains(query, ignoreCase = true) ||
                anime.genres.any { it.contains(query, ignoreCase = true) } ||
                anime.tags.any { it.contains(query, ignoreCase = true) }
            }
            Result.Success(filtered)
        } catch (e: Exception) {
            Result.Error(e, "Failed to search local anime: ${e.message}")
        }
    }

    /**
     * Imports anime from a file or directory path
     */
    suspend fun importAnimeFromFile(filePath: String): Result<Anime> {
        return try {
            val file = File(filePath)
            
            if (!file.exists()) {
                return Result.Error(
                    IllegalArgumentException("File not found"),
                    "File does not exist: $filePath"
                )
            }

            // Extract basic metadata from file/directory name
            val title = extractTitleFromPath(filePath)
            val isDirectory = file.isDirectory
            val episodes = if (isDirectory) {
                countVideoFilesInDirectory(file)
            } else {
                1 // Single file = single episode/movie
            }

            val anime = Anime(
                title = title,
                description = "Imported from: $filePath",
                totalEpisodes = episodes,
                type = if (episodes == 1) AnimeType.MOVIE else AnimeType.TV,
                source = "local",
                isLocal = true,
                localPath = filePath,
                dateAdded = Date(),
                isInLibrary = true
            )

            Result.Success(anime)
        } catch (e: Exception) {
            Result.Error(e, "Failed to import anime: ${e.message}")
        }
    }

    /**
     * Gets anime by genre
     */
    suspend fun getAnimeByGenre(genre: String): Result<List<Anime>> {
        return try {
            val filtered = sampleAnime.filter { anime ->
                anime.genres.any { it.equals(genre, ignoreCase = true) }
            }
            Result.Success(filtered)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get anime by genre: ${e.message}")
        }
    }

    /**
     * Extracts anime title from file path
     */
    private fun extractTitleFromPath(filePath: String): String {
        val file = File(filePath)
        val fileName = if (file.isDirectory) {
            file.name
        } else {
            file.nameWithoutExtension
        }

        // Basic cleanup of common filename patterns
        return fileName
            .replace(Regex("\\[.*?\\]"), "") // Remove brackets
            .replace(Regex("\\(.*?\\)"), "") // Remove parentheses
            .replace(Regex("_+"), " ") // Replace underscores with spaces
            .replace(Regex("\\s+"), " ") // Normalize spaces
            .trim()
    }

    /**
     * Counts video files in a directory
     */
    private fun countVideoFilesInDirectory(directory: File): Int {
        if (!directory.isDirectory) return 0
        
        val videoExtensions = listOf(".mp4", ".mkv", ".avi")
        return directory.listFiles()?.count { file ->
            videoExtensions.any { ext ->
                file.name.lowercase().endsWith(ext)
            }
        } ?: 0
    }

    /**
     * Generates sample episodes for an anime (for demonstration)
     * In a real implementation, this would scan the anime directory
     */
    private fun generateSampleEpisodes(anime: Anime): List<AnimeEpisode> {
        return (1..anime.totalEpisodes).map { episodeNum ->
            AnimeEpisode(
                id = "${anime.id}_episode_$episodeNum",
                animeId = anime.id,
                episodeNumber = episodeNum,
                title = if (anime.type == AnimeType.MOVIE) {
                    anime.title
                } else {
                    "Episode $episodeNum"
                },
                description = "Episode $episodeNum of ${anime.title}",
                isWatched = episodeNum <= anime.watchedEpisodes,
                watchProgress = if (episodeNum <= anime.watchedEpisodes) {
                    anime.duration * 60 * 1000L // Full episode watched
                } else {
                    0L
                },
                duration = anime.duration * 60 * 1000L, // Convert minutes to milliseconds
                isDownloaded = true,
                localPath = if (anime.type == AnimeType.MOVIE) {
                    anime.localPath
                } else {
                    "${anime.localPath}/episode_${episodeNum.toString().padStart(2, '0')}.mkv"
                },
                dateAdded = anime.dateAdded,
                source = "local"
            )
        }
    }
}