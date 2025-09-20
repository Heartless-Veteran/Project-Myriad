package com.heartlessveteran.myriad.domain.usecase

import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.services.DownloadService
import com.heartlessveteran.myriad.domain.services.DownloadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for downloading manga content.
 *
 * This use case handles the business logic for downloading manga chapters
 * from online sources, including queue management and validation.
 */
class DownloadMangaUseCase(
    private val downloadService: DownloadService,
) {
    /**
     * Download a manga and its chapters.
     *
     * @param manga The manga to download
     * @param chapters List of chapter IDs to download, or null for all chapters
     * @return Result containing the download task or error
     */
    suspend operator fun invoke(
        manga: Manga,
        chapters: List<String>? = null,
    ): Result<DownloadTask> {
        return withContext(Dispatchers.IO) {
            try {
                // Validate manga
                if (manga.id.isBlank()) {
                    return@withContext Result.Error(
                        IllegalArgumentException("Manga ID cannot be empty"),
                        "Invalid manga provided for download",
                    )
                }

                // Check if manga is from online source
                if (manga.isLocal) {
                    return@withContext Result.Error(
                        IllegalStateException("Cannot download local manga"),
                        "This manga is already stored locally",
                    )
                }

                // Delegate to DownloadService
                downloadService.enqueueMangaDownload(manga, chapters)
            } catch (e: Exception) {
                Result.Error(e, "Failed to download manga: ${e.message}")
            }
        }
    }
}
