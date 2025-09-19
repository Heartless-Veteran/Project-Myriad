package com.heartlessveteran.myriad.data.services

import android.content.Context
import android.util.Log
import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.services.DownloadService
import com.heartlessveteran.myriad.domain.services.DownloadStatus
import com.heartlessveteran.myriad.domain.services.DownloadTask
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of DownloadService for managing background downloads.
 *
 * This implementation provides:
 * - Download queue management with persistent state
 * - Background download processing with coroutines
 * - Progress tracking and status updates
 * - Concurrent download management with configurable limits
 * - WiFi-only download control
 * - Pause/resume/retry functionality
 */
class DownloadServiceImpl(
    private val context: Context,
    // TODO: Inject these dependencies when DI is fully implemented
    // private val sourceService: SourceService,
    // private val fileManagerService: FileManagerService,
    // private val downloadDao: DownloadDao
) : DownloadService {
    companion object {
        private const val TAG = "DownloadServiceImpl"
        private const val DEFAULT_MAX_CONCURRENT = 3
        private const val DOWNLOAD_DIR_NAME = "manga_downloads"
        private const val PROGRESS_UPDATE_INTERVAL = 500L // ms
    }

    // Service scope for background operations
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Download configuration
    private var maxConcurrentDownloads = DEFAULT_MAX_CONCURRENT
    private var wifiOnlyMode = true

    // Active download management
    private val activeDownloads = ConcurrentHashMap<String, Job>()
    private val _downloadQueue = MutableStateFlow<List<DownloadTask>>(emptyList())
    private val _overallProgress = MutableStateFlow(0f)
    private val _areDownloadsActive = MutableStateFlow(false)

    // Download storage directory
    private val downloadBaseDir: File
        get() =
            File(context.getExternalFilesDir(null), DOWNLOAD_DIR_NAME).apply {
                if (!exists()) mkdirs()
            }

    override suspend fun enqueueMangaDownload(
        manga: Manga,
        chapters: List<String>?,
    ): Result<DownloadTask> =
        try {
            val taskId = UUID.randomUUID().toString()
            val chapterIds = chapters ?: emptyList() // TODO: Get all chapter IDs for manga

            val downloadTask =
                DownloadTask(
                    id = taskId,
                    mangaId = manga.id,
                    mangaTitle = manga.title,
                    chapterIds = chapterIds,
                    status = DownloadStatus.QUEUED,
                )

            // Add to queue
            val currentQueue = _downloadQueue.value.toMutableList()
            currentQueue.add(downloadTask)
            _downloadQueue.value = currentQueue

            Log.i(TAG, "Enqueued download for manga: ${manga.title} with ${chapterIds.size} chapters")

            // Start processing if not at concurrent limit
            processDownloadQueue()

            Result.Success(downloadTask)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enqueue download for manga: ${manga.title}", e)
            Result.Error(e, "Failed to enqueue download: ${e.message}")
        }

    override fun getDownloadQueue(): Flow<List<DownloadTask>> = _downloadQueue.asStateFlow()

    override fun getMangaDownloads(mangaId: String): Flow<List<DownloadTask>> =
        _downloadQueue.map { tasks ->
            tasks.filter { it.mangaId == mangaId }
        }

    override suspend fun pauseDownload(taskId: String): Result<Unit> =
        try {
            activeDownloads[taskId]?.cancel()
            activeDownloads.remove(taskId)

            updateTaskStatus(taskId, DownloadStatus.PAUSED)
            Log.i(TAG, "Paused download task: $taskId")

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause download: $taskId", e)
            Result.Error(e, "Failed to pause download: ${e.message}")
        }

    override suspend fun resumeDownload(taskId: String): Result<Unit> =
        try {
            updateTaskStatus(taskId, DownloadStatus.QUEUED)
            processDownloadQueue()

            Log.i(TAG, "Resumed download task: $taskId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume download: $taskId", e)
            Result.Error(e, "Failed to resume download: ${e.message}")
        }

    override suspend fun cancelDownload(taskId: String): Result<Unit> =
        try {
            // Cancel active job if running
            activeDownloads[taskId]?.cancel()
            activeDownloads.remove(taskId)

            // Remove from queue
            val currentQueue = _downloadQueue.value.toMutableList()
            currentQueue.removeAll { it.id == taskId }
            _downloadQueue.value = currentQueue

            // Clean up any partial downloads
            cleanupPartialDownload(taskId)

            Log.i(TAG, "Cancelled download task: $taskId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel download: $taskId", e)
            Result.Error(e, "Failed to cancel download: ${e.message}")
        }

    override suspend fun retryDownload(taskId: String): Result<Unit> =
        try {
            updateTaskStatus(taskId, DownloadStatus.QUEUED, errorMessage = null)
            processDownloadQueue()

            Log.i(TAG, "Retrying download task: $taskId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retry download: $taskId", e)
            Result.Error(e, "Failed to retry download: ${e.message}")
        }

    override suspend fun clearCompletedDownloads(): Result<Unit> =
        try {
            val currentQueue = _downloadQueue.value.toMutableList()
            currentQueue.removeAll { it.status == DownloadStatus.COMPLETED }
            _downloadQueue.value = currentQueue

            Log.i(TAG, "Cleared completed downloads")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear completed downloads", e)
            Result.Error(e, "Failed to clear completed downloads: ${e.message}")
        }

    override fun getOverallProgress(): Flow<Float> = _overallProgress.asStateFlow()

    override fun areDownloadsActive(): Flow<Boolean> = _areDownloadsActive.asStateFlow()

    override suspend fun setDownloadPreferences(
        maxConcurrent: Int,
        wifiOnly: Boolean,
    ): Result<Unit> =
        try {
            maxConcurrentDownloads = maxConcurrent
            wifiOnlyMode = wifiOnly

            Log.i(TAG, "Updated download preferences: maxConcurrent=$maxConcurrent, wifiOnly=$wifiOnly")

            // Restart processing with new limits
            processDownloadQueue()

            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set download preferences", e)
            Result.Error(e, "Failed to set download preferences: ${e.message}")
        }

    /**
     * Process the download queue and start downloads up to the concurrent limit.
     */
    private fun processDownloadQueue() {
        serviceScope.launch {
            val queuedTasks =
                _downloadQueue.value.filter { task ->
                    task.status == DownloadStatus.QUEUED && !activeDownloads.containsKey(task.id)
                }

            val availableSlots = maxConcurrentDownloads - activeDownloads.size
            val tasksToStart = queuedTasks.take(availableSlots)

            tasksToStart.forEach { task ->
                startDownloadTask(task)
            }

            updateGlobalState()
        }
    }

    /**
     * Start downloading a specific task.
     */
    private fun startDownloadTask(task: DownloadTask) {
        val downloadJob =
            serviceScope.launch {
                try {
                    updateTaskStatus(task.id, DownloadStatus.IN_PROGRESS, startedAt = System.currentTimeMillis())

                    // TODO: Implement actual download logic
                    // This would involve:
                    // 1. Getting manga chapters from source service
                    // 2. Downloading each chapter's pages
                    // 3. Organizing files in download directory
                    // 4. Updating progress throughout the process

                    // Placeholder implementation for now
                    simulateDownload(task)

                    updateTaskStatus(task.id, DownloadStatus.COMPLETED, completedAt = System.currentTimeMillis())
                    Log.i(TAG, "Completed download for manga: ${task.mangaTitle}")
                } catch (e: CancellationException) {
                    Log.i(TAG, "Download cancelled for manga: ${task.mangaTitle}")
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Download failed for manga: ${task.mangaTitle}", e)
                    updateTaskStatus(task.id, DownloadStatus.FAILED, errorMessage = e.message)
                } finally {
                    activeDownloads.remove(task.id)
                    updateGlobalState()
                    // Process next tasks in queue
                    processDownloadQueue()
                }
            }

        activeDownloads[task.id] = downloadJob
    }

    /**
     * Simulate download progress for demonstration purposes.
     * TODO: Replace with actual download implementation.
     */
    private suspend fun simulateDownload(task: DownloadTask) {
        val totalSteps = 10
        for (step in 1..totalSteps) {
            delay(1000) // Simulate download time

            val progress = step.toFloat() / totalSteps
            val downloadedBytes = (progress * 10_000_000).toLong() // Simulate 10MB download

            updateTaskProgress(task.id, progress, downloadedBytes, 10_000_000L)

            Log.d(TAG, "Download progress for ${task.mangaTitle}: ${(progress * 100).toInt()}%")
        }
    }

    /**
     * Update the status of a download task.
     */
    private fun updateTaskStatus(
        taskId: String,
        status: DownloadStatus,
        errorMessage: String? = null,
        startedAt: Long? = null,
        completedAt: Long? = null,
    ) {
        val currentQueue = _downloadQueue.value.toMutableList()
        val taskIndex = currentQueue.indexOfFirst { it.id == taskId }

        if (taskIndex >= 0) {
            val task = currentQueue[taskIndex]
            currentQueue[taskIndex] =
                task.copy(
                    status = status,
                    errorMessage = errorMessage,
                    startedAt = startedAt ?: task.startedAt,
                    completedAt = completedAt ?: task.completedAt,
                )
            _downloadQueue.value = currentQueue
        }
    }

    /**
     * Update the progress of a download task.
     */
    private fun updateTaskProgress(
        taskId: String,
        progress: Float,
        downloadedBytes: Long,
        totalBytes: Long,
    ) {
        val currentQueue = _downloadQueue.value.toMutableList()
        val taskIndex = currentQueue.indexOfFirst { it.id == taskId }

        if (taskIndex >= 0) {
            val task = currentQueue[taskIndex]
            currentQueue[taskIndex] =
                task.copy(
                    progress = progress,
                    downloadedBytes = downloadedBytes,
                    totalBytes = totalBytes,
                )
            _downloadQueue.value = currentQueue
        }
    }

    /**
     * Update global download state (overall progress and active status).
     */
    private fun updateGlobalState() {
        val currentTasks = _downloadQueue.value
        val activeTasks = currentTasks.filter { it.status == DownloadStatus.IN_PROGRESS }

        // Update active status
        _areDownloadsActive.value = activeTasks.isNotEmpty()

        // Calculate overall progress
        if (activeTasks.isNotEmpty()) {
            val totalProgress = activeTasks.sumOf { it.progress.toDouble() }
            _overallProgress.value = (totalProgress / activeTasks.size).toFloat()
        } else {
            _overallProgress.value = 0f
        }
    }

    /**
     * Clean up partial download files for a cancelled task.
     */
    private fun cleanupPartialDownload(taskId: String) {
        try {
            val taskDir = File(downloadBaseDir, taskId)
            if (taskDir.exists()) {
                taskDir.deleteRecursively()
                Log.d(TAG, "Cleaned up partial download for task: $taskId")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cleanup partial download for task: $taskId", e)
        }
    }

    /**
     * Clean up resources when service is destroyed.
     */
    fun cleanup() {
        serviceScope.cancel()
        activeDownloads.clear()
    }
}
