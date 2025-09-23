package com.heartlessveteran.myriad.feature.vault.data.download

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Download manager for handling manga downloads in the background.
 * Uses WorkManager for reliable background processing.
 */
class DownloadManager(
    private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)
    
    private val _downloadQueue = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloadQueue: StateFlow<List<DownloadItem>> = _downloadQueue.asStateFlow()
    
    private val _activeDownloads = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val activeDownloads: StateFlow<Map<String, DownloadProgress>> = _activeDownloads.asStateFlow()

    /**
     * Add a download to the queue
     */
    fun addDownload(item: DownloadItem) {
        val currentQueue = _downloadQueue.value.toMutableList()
        
        // Check if already in queue
        if (currentQueue.none { it.id == item.id }) {
            currentQueue.add(item)
            _downloadQueue.value = currentQueue
            
            // Start the download
            startDownload(item)
        }
    }

    /**
     * Start downloading an item
     */
    private fun startDownload(item: DownloadItem) {
        val downloadData = workDataOf(
            "download_id" to item.id,
            "download_url" to item.url,
            "download_title" to item.title,
            "download_type" to item.type.name
        )

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(downloadData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueue(downloadRequest)
        
        // Update active downloads
        updateDownloadProgress(item.id, DownloadProgress(
            id = item.id,
            status = DownloadStatus.QUEUED,
            progress = 0f,
            bytesDownloaded = 0,
            totalBytes = 0
        ))
    }

    /**
     * Pause a download
     */
    fun pauseDownload(downloadId: String) {
        workManager.cancelAllWorkByTag(downloadId)
        updateDownloadStatus(downloadId, DownloadStatus.PAUSED)
    }

    /**
     * Resume a paused download
     */
    fun resumeDownload(downloadId: String) {
        val item = _downloadQueue.value.find { it.id == downloadId }
        if (item != null) {
            startDownload(item)
        }
    }

    /**
     * Cancel a download
     */
    fun cancelDownload(downloadId: String) {
        workManager.cancelAllWorkByTag(downloadId)
        
        // Remove from queue
        val currentQueue = _downloadQueue.value.toMutableList()
        currentQueue.removeAll { it.id == downloadId }
        _downloadQueue.value = currentQueue
        
        // Remove from active downloads
        val currentActive = _activeDownloads.value.toMutableMap()
        currentActive.remove(downloadId)
        _activeDownloads.value = currentActive
    }

    /**
     * Cancel all downloads
     */
    fun cancelAllDownloads() {
        workManager.cancelAllWork()
        _downloadQueue.value = emptyList()
        _activeDownloads.value = emptyMap()
    }

    /**
     * Update download progress
     */
    internal fun updateDownloadProgress(downloadId: String, progress: DownloadProgress) {
        val currentActive = _activeDownloads.value.toMutableMap()
        currentActive[downloadId] = progress
        _activeDownloads.value = currentActive
    }

    /**
     * Update download status
     */
    private fun updateDownloadStatus(downloadId: String, status: DownloadStatus) {
        val currentActive = _activeDownloads.value.toMutableMap()
        val current = currentActive[downloadId]
        if (current != null) {
            currentActive[downloadId] = current.copy(status = status)
            _activeDownloads.value = currentActive
        }
    }

    /**
     * Get download progress for a specific item
     */
    fun getDownloadProgress(downloadId: String): DownloadProgress? {
        return _activeDownloads.value[downloadId]
    }

    /**
     * Check if item is being downloaded
     */
    fun isDownloading(downloadId: String): Boolean {
        return _activeDownloads.value[downloadId]?.status == DownloadStatus.DOWNLOADING
    }
}

/**
 * Represents an item to be downloaded
 */
data class DownloadItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val type: DownloadType,
    val destinationPath: String? = null,
    val priority: Int = 0,
    val headers: Map<String, String> = emptyMap()
)

/**
 * Download progress information
 */
data class DownloadProgress(
    val id: String,
    val status: DownloadStatus,
    val progress: Float, // 0.0 to 1.0
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val error: String? = null
)

/**
 * Download status enumeration
 */
enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Types of downloadable content
 */
enum class DownloadType {
    MANGA_CHAPTER,
    MANGA_VOLUME,
    ANIME_EPISODE,
    ANIME_SEASON
}