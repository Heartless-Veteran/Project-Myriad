package com.heartlessveteran.myriad.data.repository

import com.heartlessveteran.myriad.domain.entities.AnimeEpisode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Local implementation of anime episode repository for progress tracking
 * In a real app, this would use Room database and the existing AnimeEpisode entity
 */
class LocalAnimeEpisodeRepository {
    
    // In-memory storage for demo purposes
    private val _episodes = MutableStateFlow<List<AnimeEpisode>>(emptyList())
    val episodes: Flow<List<AnimeEpisode>> = _episodes.asStateFlow()
    
    // Sample episodes for testing
    init {
        _episodes.value = listOf(
            AnimeEpisode(
                id = "anime_1",
                animeId = "attack_on_titan",
                episodeNumber = 1,
                title = "To You, in 2000 Years",
                duration = 1472000L, // 24:32 in milliseconds
                watchProgress = 0L,
                localPath = "android.resource://com.heartlessveteran.myriad/raw/sample_video_1"
            ),
            AnimeEpisode(
                id = "anime_2", 
                animeId = "death_note",
                episodeNumber = 1,
                title = "Rebirth",
                duration = 1395000L, // 23:15 in milliseconds
                watchProgress = 418500L, // 30% watched
                localPath = "android.resource://com.heartlessveteran.myriad/raw/sample_video_2"
            ),
            AnimeEpisode(
                id = "anime_3",
                animeId = "naruto", 
                episodeNumber = 1,
                title = "Enter: Naruto Uzumaki!",
                duration = 1388000L, // 23:08 in milliseconds
                watchProgress = 1388000L, // Fully watched
                localPath = "android.resource://com.heartlessveteran.myriad/raw/sample_video_3"
            ),
            AnimeEpisode(
                id = "anime_4",
                animeId = "one_piece",
                episodeNumber = 1, 
                title = "I'm Luffy! The Man Who's Gonna Be King of the Pirates!",
                duration = 1485000L, // 24:45 in milliseconds
                watchProgress = 1039500L, // 70% watched
                localPath = "android.resource://com.heartlessveteran.myriad/raw/sample_video_4"
            )
        )
    }
    
    suspend fun getEpisodeById(id: String): AnimeEpisode? {
        return _episodes.value.find { it.id == id }
    }
    
    suspend fun updateWatchProgress(episodeId: String, progress: Long) {
        val currentEpisodes = _episodes.value.toMutableList()
        val episodeIndex = currentEpisodes.indexOfFirst { it.id == episodeId }
        
        if (episodeIndex != -1) {
            val episode = currentEpisodes[episodeIndex]
            currentEpisodes[episodeIndex] = episode.copy(
                watchProgress = progress,
                isWatched = progress >= episode.duration * 0.9 // Mark as watched if 90% complete
            )
            _episodes.value = currentEpisodes
        }
    }
    
    suspend fun getAllEpisodes(): List<AnimeEpisode> {
        return _episodes.value
    }
}