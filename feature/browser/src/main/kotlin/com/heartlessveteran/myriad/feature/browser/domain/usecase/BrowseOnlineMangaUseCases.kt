package com.heartlessveteran.myriad.feature.browser.domain.usecase

import com.heartlessveteran.myriad.core.domain.entities.Manga
import com.heartlessveteran.myriad.core.domain.entities.MangaChapter
import com.heartlessveteran.myriad.core.domain.manager.PluginManager
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.repository.MangaRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for browsing and interacting with online manga sources.
 * Handles discovery, search, and downloading of online manga.
 */
class BrowseOnlineMangaUseCase(
    private val pluginManager: PluginManager,
    private val mangaRepository: MangaRepository
) {
    /**
     * Get popular manga from MangaDx
     */
    suspend fun getPopularManga(page: Int = 0): Result<List<Manga>> {
        return try {
            val mangaDxSource = pluginManager.getSourceByPluginId("mangadx")
            when (mangaDxSource) {
                is Result.Success -> {
                    mangaDxSource.data.getPopularManga(page)
                }
                is Result.Error -> {
                    Result.Error(mangaDxSource.exception, mangaDxSource.message ?: "Failed to get MangaDx source")
                }
                is Result.Loading -> {
                    Result.Error(Exception("Source is loading"), "Source is still loading")
                }
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get popular manga: ${e.message}")
        }
    }

    /**
     * Get latest manga from MangaDx
     */
    suspend fun getLatestManga(page: Int = 0): Result<List<Manga>> {
        return try {
            val mangaDxSource = pluginManager.getSourceByPluginId("mangadx")
            when (mangaDxSource) {
                is Result.Success -> {
                    mangaDxSource.data.getLatestManga(page)
                }
                is Result.Error -> {
                    Result.Error(mangaDxSource.exception, mangaDxSource.message ?: "Failed to get MangaDx source")
                }
                is Result.Loading -> {
                    Result.Error(Exception("Source is loading"), "Source is still loading")
                }
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to get latest manga: ${e.message}")
        }
    }

    /**
     * Search manga on MangaDx
     */
    suspend fun searchManga(query: String, page: Int = 0): Result<List<Manga>> {
        return try {
            val mangaDxSource = pluginManager.getSourceByPluginId("mangadx")
            when (mangaDxSource) {
                is Result.Success -> {
                    mangaDxSource.data.searchManga(query, page)
                }
                is Result.Error -> {
                    Result.Error(mangaDxSource.exception, mangaDxSource.message ?: "Failed to get MangaDx source")
                }
                is Result.Loading -> {
                    Result.Error(Exception("Source is loading"), "Source is still loading")
                }
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to search manga: ${e.message}")
        }
    }

    /**
     * Add manga to local library
     */
    suspend fun addToLibrary(manga: Manga): Result<Unit> {
        return try {
            val updatedManga = manga.copy(
                isInLibrary = true,
                dateAdded = java.util.Date()
            )
            mangaRepository.saveManga(updatedManga)
        } catch (e: Exception) {
            Result.Error(e, "Failed to add manga to library: ${e.message}")
        }
    }
}