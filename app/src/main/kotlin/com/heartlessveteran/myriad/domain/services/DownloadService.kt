package com.heartlessveteran.myriad.domain.services

import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.models.Result
import kotlinx.coroutines.flow.Flow

/**
 * Download management service for handling background downloads.
 *
 * This service provides functionality for:
 * - Managing download queues
 * - Background download processing
 * - Progress tracking and notifications
 * - Storage management for downloaded content
 */
interface DownloadService {
    /**
     * Enqueue a manga for download.
     *
     * @param manga The manga to download
     * @param chapters List of chapter IDs to download, or null for all
     * @return Result containing the created DownloadTask or an error
     */
    suspend fun enqueueMangaDownload(
        manga: Manga,
        chapters: List<String>? = null,
    ): Result<DownloadTask>

    /**
     * Get all active download tasks as a Flow.
     *
     * @return Flow emitting the current download queue
     */
    fun getDownloadQueue(): Flow<List<DownloadTask>>

    /**
     * Get download tasks for a specific manga.
     *
     * @param mangaId The manga ID
     * @return Flow emitting download tasks for the manga
     */
    fun getMangaDownloads(mangaId: String): Flow<List<DownloadTask>>

    /**
     * Pause a download task.
     *
     * @param taskId The download task ID
     * @return Result indicating success or failure
     */
    suspend fun pauseDownload(taskId: String): Result<Unit>

    /**
     * Resume a paused download task.
     *
     * @param taskId The download task ID
     * @return Result indicating success or failure
     */
    suspend fun resumeDownload(taskId: String): Result<Unit>

    /**
     * Cancel a download task and remove it from the queue.
     *
     * @param taskId The download task ID
     * @return Result indicating success or failure
     */
    suspend fun cancelDownload(taskId: String): Result<Unit>

    /**
     * Retry a failed download task.
     *
     * @param taskId The download task ID
     * @return Result indicating success or failure
     */
    suspend fun retryDownload(taskId: String): Result<Unit>

    /**
     * Clear all completed download tasks from the queue.
     *
     * @return Result indicating success or failure
     */
    suspend fun clearCompletedDownloads(): Result<Unit>

    /**
     * Get total download progress for all active tasks.
     *
     * @return Flow emitting overall progress (0.0 to 1.0)
     */
    fun getOverallProgress(): Flow<Float>

    /**
     * Check if downloads are currently active.
     *
     * @return Flow emitting true if any downloads are in progress
     */
    fun areDownloadsActive(): Flow<Boolean>

    /**
     * Set download preferences (max concurrent downloads, etc).
     *
     * @param maxConcurrent Maximum concurrent downloads
     * @param wifiOnly Only download over WiFi
     * @return Result indicating success or failure
     */
    suspend fun setDownloadPreferences(
        maxConcurrent: Int = 3,
        wifiOnly: Boolean = true,
    ): Result<Unit>
}

/**
 * Represents a download task in the queue.
 */
data class DownloadTask(
    val id: String,
    val mangaId: String,
    val mangaTitle: String,
    val chapterIds: List<String>,
    val status: DownloadStatus,
    val progress: Float = 0f,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
)

/**
 * Status of a download task.
 */
enum class DownloadStatus {
    QUEUED,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED,
}
