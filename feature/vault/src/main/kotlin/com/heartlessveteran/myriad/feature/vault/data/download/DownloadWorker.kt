package com.heartlessveteran.myriad.feature.vault.data.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * WorkManager worker for handling background downloads.
 * Performs the actual download work and reports progress.
 */
class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val downloadId = inputData.getString("download_id") ?: return@withContext Result.failure()
                val downloadUrl = inputData.getString("download_url") ?: return@withContext Result.failure()
                val downloadTitle = inputData.getString("download_title") ?: "Unknown"
                val downloadType = inputData.getString("download_type") ?: "MANGA_CHAPTER"

                downloadFile(downloadId, downloadUrl, downloadTitle)
                
                Result.success(workDataOf(
                    "download_id" to downloadId,
                    "status" to "COMPLETED"
                ))
            } catch (e: Exception) {
                Result.failure(workDataOf(
                    "error" to e.message
                ))
            }
        }
    }

    private suspend fun downloadFile(downloadId: String, urlString: String, title: String) {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.connect()
            
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("HTTP ${connection.responseCode}: ${connection.responseMessage}")
            }

            val totalBytes = connection.contentLength.toLong()
            val inputStream = connection.inputStream
            
            // Create download directory
            val downloadDir = File(applicationContext.getExternalFilesDir(null), "downloads")
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            
            // Create file with safe filename
            val fileName = "${title.replace(Regex("[^a-zA-Z0-9.-]"), "_")}.cbz"
            val outputFile = File(downloadDir, fileName)
            val outputStream = FileOutputStream(outputFile)
            
            val buffer = ByteArray(8192)
            var bytesDownloaded = 0L
            var bytesRead: Int
            
            // Update initial progress
            updateProgress(downloadId, DownloadStatus.DOWNLOADING, 0f, 0, totalBytes)
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                bytesDownloaded += bytesRead
                
                // Update progress
                val progress = if (totalBytes > 0) {
                    bytesDownloaded.toFloat() / totalBytes.toFloat()
                } else 0f
                
                updateProgress(downloadId, DownloadStatus.DOWNLOADING, progress, bytesDownloaded, totalBytes)
            }
            
            outputStream.close()
            inputStream.close()
            
            // Mark as completed
            updateProgress(downloadId, DownloadStatus.COMPLETED, 1f, bytesDownloaded, totalBytes)
            
        } finally {
            connection.disconnect()
        }
    }

    private fun updateProgress(
        downloadId: String,
        status: DownloadStatus,
        progress: Float,
        bytesDownloaded: Long,
        totalBytes: Long
    ) {
        // In a real implementation, this would communicate with the DownloadManager
        // For now, we'll use a simple notification or local broadcast
        setProgressAsync(workDataOf(
            "download_id" to downloadId,
            "status" to status.name,
            "progress" to progress,
            "bytes_downloaded" to bytesDownloaded,
            "total_bytes" to totalBytes
        ))
    }
}