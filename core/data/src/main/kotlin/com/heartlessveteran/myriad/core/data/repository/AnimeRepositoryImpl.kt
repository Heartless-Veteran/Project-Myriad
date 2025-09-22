package com.heartlessveteran.myriad.core.data.repository

import com.heartlessveteran.myriad.core.data.source.LocalAnimeSource
import com.heartlessveteran.myriad.core.domain.entities.Anime
import com.heartlessveteran.myriad.core.domain.entities.AnimeEpisode
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.AnimeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AnimeRepository interface.
 * Handles data operations for anime using local and remote sources.
 * Follows Clean Architecture principles by implementing domain interface.
 */
@Singleton
class AnimeRepositoryImpl @Inject constructor(
    private val localAnimeSource: LocalAnimeSource
) : AnimeRepository {

    // In-memory cache for anime library
    private val _libraryAnime = MutableStateFlow<List<Anime>>(emptyList())
    private val _animeEpisodes = MutableStateFlow<Map<String, List<AnimeEpisode>>>(emptyMap())

    init {
        // Initialize with sample data
        // In a real implementation, this would load from database
        loadInitialData()
    }

    override fun getLibraryAnime(): Flow<List<Anime>> {
        return _libraryAnime.asStateFlow()
    }

    override suspend fun getAnimeById(id: String): Result<Anime> {
        return try {
            // First check in-memory cache
            val cachedAnime = _libraryAnime.value.find { it.id == id }
            if (cachedAnime != null) {
                Result.Success(cachedAnime)
            } else {
                // Fallback to local source
                localAnimeSource.getAnimeById(id)
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get anime by ID: ${e.message}")
        }
    }

    override suspend fun saveAnime(anime: Anime): Result<Unit> {
        return try {
            // Update in-memory cache
            val currentAnime = _libraryAnime.value.toMutableList()
            val existingIndex = currentAnime.indexOfFirst { it.id == anime.id }
            
            if (existingIndex >= 0) {
                currentAnime[existingIndex] = anime
            } else {
                currentAnime.add(anime)
            }
            
            _libraryAnime.value = currentAnime
            
            // In a real implementation, this would also save to database
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to save anime: ${e.message}")
        }
    }

    override suspend fun removeAnime(animeId: String): Result<Unit> {
        return try {
            val currentAnime = _libraryAnime.value.toMutableList()
            val removed = currentAnime.removeAll { it.id == animeId }
            
            if (removed) {
                _libraryAnime.value = currentAnime
                
                // Also remove episodes from cache
                val currentEpisodes = _animeEpisodes.value.toMutableMap()
                currentEpisodes.remove(animeId)
                _animeEpisodes.value = currentEpisodes
                
                Result.Success(Unit)
            } else {
                Result.Error(
                    IllegalArgumentException("Anime not found"),
                    "No anime found with ID: $animeId"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to remove anime: ${e.message}")
        }
    }

    override suspend fun updateWatchingProgress(animeId: String, watchedEpisodes: Int): Result<Unit> {
        return try {
            val currentAnime = _libraryAnime.value.toMutableList()
            val animeIndex = currentAnime.indexOfFirst { it.id == animeId }
            
            if (animeIndex >= 0) {
                val updatedAnime = currentAnime[animeIndex].copy(
                    watchedEpisodes = watchedEpisodes,
                    lastWatchedDate = java.util.Date()
                )
                currentAnime[animeIndex] = updatedAnime
                _libraryAnime.value = currentAnime
                
                Result.Success(Unit)
            } else {
                Result.Error(
                    IllegalArgumentException("Anime not found"),
                    "No anime found with ID: $animeId"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to update watching progress: ${e.message}")
        }
    }

    override suspend fun toggleFavorite(animeId: String): Result<Anime> {
        return try {
            val currentAnime = _libraryAnime.value.toMutableList()
            val animeIndex = currentAnime.indexOfFirst { it.id == animeId }
            
            if (animeIndex >= 0) {
                val anime = currentAnime[animeIndex]
                val updatedAnime = anime.copy(isFavorite = !anime.isFavorite)
                currentAnime[animeIndex] = updatedAnime
                _libraryAnime.value = currentAnime
                
                Result.Success(updatedAnime)
            } else {
                Result.Error(
                    IllegalArgumentException("Anime not found"),
                    "No anime found with ID: $animeId"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to toggle favorite: ${e.message}")
        }
    }

    override fun searchLibraryAnime(query: String): Flow<List<Anime>> {
        return _libraryAnime.map { anime ->
            if (query.isBlank()) {
                anime
            } else {
                anime.filter { 
                    it.title.contains(query, ignoreCase = true) ||
                    it.alternativeTitles.any { title -> title.contains(query, ignoreCase = true) } ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.studio.contains(query, ignoreCase = true) ||
                    it.genres.any { genre -> genre.contains(query, ignoreCase = true) }
                }
            }
        }
    }

    override fun getAnimeByGenre(genre: String): Flow<List<Anime>> {
        return _libraryAnime.map { anime ->
            anime.filter { it.genres.any { g -> g.equals(genre, ignoreCase = true) } }
        }
    }

    override fun getRecentlyWatchedAnime(limit: Int): Flow<List<Anime>> {
        return _libraryAnime.map { anime ->
            anime
                .filter { it.lastWatchedDate != null }
                .sortedByDescending { it.lastWatchedDate }
                .take(limit)
        }
    }

    override fun getEpisodesForAnime(animeId: String): Flow<List<AnimeEpisode>> {
        return flow {
            // Check cache first
            val cachedEpisodes = _animeEpisodes.value[animeId]
            if (cachedEpisodes != null) {
                emit(cachedEpisodes)
            } else {
                // Load from source
                when (val result = localAnimeSource.getEpisodesForAnime(animeId)) {
                    is Result.Success -> {
                        // Cache the episodes
                        val currentCache = _animeEpisodes.value.toMutableMap()
                        currentCache[animeId] = result.data
                        _animeEpisodes.value = currentCache
                        
                        emit(result.data)
                    }
                    is Result.Error -> {
                        emit(emptyList())
                    }
                    is Result.Loading -> {
                        emit(emptyList())
                    }
                }
            }
        }
    }

    override suspend fun saveEpisode(episode: AnimeEpisode): Result<Unit> {
        return try {
            val currentCache = _animeEpisodes.value.toMutableMap()
            val animeEpisodes = currentCache[episode.animeId]?.toMutableList() ?: mutableListOf()
            
            val existingIndex = animeEpisodes.indexOfFirst { it.id == episode.id }
            if (existingIndex >= 0) {
                animeEpisodes[existingIndex] = episode
            } else {
                animeEpisodes.add(episode)
            }
            
            currentCache[episode.animeId] = animeEpisodes
            _animeEpisodes.value = currentCache
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to save episode: ${e.message}")
        }
    }

    override suspend fun updateEpisodeProgress(
        episodeId: String,
        isWatched: Boolean,
        watchProgress: Long
    ): Result<Unit> {
        return try {
            val currentCache = _animeEpisodes.value.toMutableMap()
            var updated = false
            
            for ((animeId, episodes) in currentCache) {
                val episodeIndex = episodes.indexOfFirst { it.id == episodeId }
                if (episodeIndex >= 0) {
                    val updatedEpisodes = episodes.toMutableList()
                    val updatedEpisode = episodes[episodeIndex].copy(
                        isWatched = isWatched,
                        watchProgress = watchProgress,
                        dateWatched = if (isWatched) java.util.Date() else null
                    )
                    updatedEpisodes[episodeIndex] = updatedEpisode
                    currentCache[animeId] = updatedEpisodes
                    updated = true
                    
                    // Update anime watching progress
                    val watchedCount = updatedEpisodes.count { it.isWatched }
                    updateWatchingProgress(animeId, watchedCount)
                    break
                }
            }
            
            if (updated) {
                _animeEpisodes.value = currentCache
                Result.Success(Unit)
            } else {
                Result.Error(
                    IllegalArgumentException("Episode not found"),
                    "No episode found with ID: $episodeId"
                )
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to update episode progress: ${e.message}")
        }
    }

    override suspend fun getNextUnwatchedEpisode(animeId: String): Result<AnimeEpisode?> {
        return try {
            when (val episodesResult = localAnimeSource.getEpisodesForAnime(animeId)) {
                is Result.Success -> {
                    val nextEpisode = episodesResult.data
                        .filter { !it.isWatched }
                        .minByOrNull { it.episodeNumber }
                    
                    Result.Success(nextEpisode)
                }
                is Result.Error -> {
                    Result.Error(episodesResult.exception, episodesResult.message)
                }
                is Result.Loading -> {
                    Result.Success(null)
                }
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get next unwatched episode: ${e.message}")
        }
    }

    override suspend fun importAnimeFromFile(filePath: String): Result<Anime> {
        return when (val result = localAnimeSource.importAnimeFromFile(filePath)) {
            is Result.Success -> {
                // Add to library
                saveAnime(result.data)
                result
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    private fun loadInitialData() {
        // In a real implementation, this would load from database
        // For now, we'll load sample data from the local source
        try {
            // This would be done asynchronously in a real implementation
            CoroutineScope(Dispatchers.IO).launch {
                when (val result = localAnimeSource.getLocalAnime()) {
                    is Result.Success -> {
                        _libraryAnime.value = result.data
                    }
                    else -> {
                        // Handle error or loading state
                    }
                }
            }
        } catch (e: Exception) {
            // Handle initialization error
        }
    }
}