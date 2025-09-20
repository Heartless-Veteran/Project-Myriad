package com.heartlessveteran.myriad.data.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.heartlessveteran.myriad.MainActivity
import com.heartlessveteran.myriad.R
import com.heartlessveteran.myriad.domain.services.DownloadService
import com.heartlessveteran.myriad.domain.services.DownloadStatus
import com.heartlessveteran.myriad.domain.services.DownloadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service for handling background downloads.
 *
 * This service ensures downloads continue running even when the app is in the background
 * or closed. It provides persistent notifications showing download progress and allows
 * users to manage downloads from the notification panel.
 */
class DownloadForegroundService : Service() {
    
    companion object {
        private const val TAG = "DownloadForegroundService"
        private const val NOTIFICATION_CHANNEL_ID = "download_channel"
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_CODE_OPEN_APP = 1004
        private const val REQUEST_CODE_PAUSE_ALL = 1005
        private const val REQUEST_CODE_CANCEL_ALL = 1006
        
        const val ACTION_START_DOWNLOADS = "START_DOWNLOADS"
        const val ACTION_PAUSE_ALL = "PAUSE_ALL"
        const val ACTION_CANCEL_ALL = "CANCEL_ALL"
        const val ACTION_STOP_SERVICE = "STOP_SERVICE"
        
        /**
         * Start the download foreground service.
         */
        fun startService(context: Context) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_START_DOWNLOADS
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        /**
         * Stop the download foreground service.
         */
        fun stopService(context: Context) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.stopService(intent)
        }
    }
    
    private lateinit var downloadService: DownloadService
    private lateinit var notificationManager: NotificationManagerCompat
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var downloadMonitoringJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Download foreground service created")
        
        // Initialize download service (normally would be injected via DI)
        downloadService = DownloadServiceImpl(this)
        
        // Setup notification manager
        notificationManager = NotificationManagerCompat.from(this)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DOWNLOADS -> {
                Log.d(TAG, "Starting download monitoring")
                startForegroundWithNotification()
                startDownloadMonitoring()
            }
            ACTION_PAUSE_ALL -> {
                Log.d(TAG, "Pausing all downloads")
                pauseAllDownloads()
            }
            ACTION_CANCEL_ALL -> {
                Log.d(TAG, "Cancelling all downloads")
                cancelAllDownloads()
            }
            ACTION_STOP_SERVICE -> {
                Log.d(TAG, "Stopping service")
                stopSelf()
            }
        }
        
        // Service will be restarted if killed by system
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        Log.d(TAG, "Download foreground service destroyed")
        downloadMonitoringJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background download notifications"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun startForegroundWithNotification() {
        val notification = buildNotification(
            title = "Downloads Starting",
            text = "Preparing download queue...",
            progress = 0,
            indeterminate = true
        )
        
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun startDownloadMonitoring() {
        downloadMonitoringJob?.cancel()
        downloadMonitoringJob = serviceScope.launch {
            downloadService.getDownloadQueue().collect { queue ->
                updateNotificationForQueue(queue)
                
                // Stop service if no active downloads
                val hasActiveDownloads = queue.any { task ->
                    task.status == DownloadStatus.IN_PROGRESS || 
                    task.status == DownloadStatus.QUEUED
                }
                
                if (!hasActiveDownloads && queue.isNotEmpty()) {
                    Log.d(TAG, "No active downloads, stopping service")
                    stopSelf()
                }
            }
        }
    }
    
    private fun updateNotificationForQueue(queue: List<DownloadTask>) {
        val activeDownloads = queue.filter { 
            it.status == DownloadStatus.IN_PROGRESS || it.status == DownloadStatus.QUEUED 
        }
        val completedDownloads = queue.count { it.status == DownloadStatus.COMPLETED }
        val failedDownloads = queue.count { it.status == DownloadStatus.FAILED }
        
        val notification = when {
            activeDownloads.isEmpty() -> buildCompletionNotification(completedDownloads, failedDownloads)
            activeDownloads.size == 1 -> buildSingleDownloadNotification(activeDownloads.first())
            else -> buildMultipleDownloadNotification(activeDownloads, completedDownloads)
        }
        
        updateNotification(notification)
    }
    
    private fun buildSingleDownloadNotification(task: DownloadTask): Notification {
        return buildNotification(
            title = "Downloading: ${task.mangaTitle}",
            text = when (task.status) {
                DownloadStatus.IN_PROGRESS -> "${task.chapterIds.size} chapters • ${(task.progress * 100).toInt()}%"
                DownloadStatus.QUEUED -> "Waiting to start..."
                else -> "Processing..."
            },
            progress = (task.progress * 100).toInt(),
            indeterminate = task.status == DownloadStatus.QUEUED
        )
    }
    
    private fun buildMultipleDownloadNotification(
        activeDownloads: List<DownloadTask>,
        completedCount: Int
    ): Notification {
        val totalProgress = activeDownloads.map { it.progress }.average()
        
        return buildNotification(
            title = "${activeDownloads.size} downloads active",
            text = if (completedCount > 0) {
                "$completedCount completed • ${(totalProgress * 100).toInt()}% overall"
            } else {
                "${(totalProgress * 100).toInt()}% overall progress"
            },
            progress = (totalProgress * 100).toInt(),
            indeterminate = false
        )
    }
    
    private fun buildCompletionNotification(completed: Int, failed: Int): Notification {
        val title = when {
            failed > 0 && completed > 0 -> "Downloads completed with errors"
            failed > 0 -> "Downloads failed"
            else -> "Downloads completed"
        }
        
        val text = when {
            failed > 0 && completed > 0 -> "$completed completed, $failed failed"
            failed > 0 -> "$failed downloads failed"
            else -> "$completed downloads completed"
        }
        
        return buildNotification(
            title = title,
            text = text,
            progress = 100,
            indeterminate = false
        )
    }
    
    private fun buildNotification(
        title: String,
        text: String,
        progress: Int,
        indeterminate: Boolean
    ): Notification {
        // Create intent to open app
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, REQUEST_CODE_OPEN_APP, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create pause all intent
        val pauseAllIntent = Intent(this, DownloadForegroundService::class.java).apply {
            action = ACTION_PAUSE_ALL
        }
        val pauseAllPendingIntent = PendingIntent.getService(
            this, REQUEST_CODE_PAUSE_ALL, pauseAllIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create cancel all intent
        val cancelAllIntent = Intent(this, DownloadForegroundService::class.java).apply {
            action = ACTION_CANCEL_ALL
        }
        val cancelAllPendingIntent = PendingIntent.getService(
            this, REQUEST_CODE_CANCEL_ALL, cancelAllIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setProgress(100, progress, indeterminate)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Pause All",
                pauseAllPendingIntent
            )
            .addAction(
                android.R.drawable.ic_delete,
                "Cancel All", 
                cancelAllPendingIntent
            )
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    private fun updateNotification(notification: Notification) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }
    
    private fun pauseAllDownloads() {
        serviceScope.launch {
            val currentQueue = mutableListOf<DownloadTask>()
            downloadService.getDownloadQueue().collect { queue ->
                currentQueue.clear()
                currentQueue.addAll(queue)
                return@collect
            }
            
            currentQueue.forEach { task ->
                if (task.status == DownloadStatus.IN_PROGRESS || task.status == DownloadStatus.QUEUED) {
                    downloadService.pauseDownload(task.id)
                }
            }
        }
    }
    
    private fun cancelAllDownloads() {
        serviceScope.launch {
            val currentQueue = mutableListOf<DownloadTask>()
            downloadService.getDownloadQueue().collect { queue ->
                currentQueue.clear()
                currentQueue.addAll(queue)
                return@collect
            }
            
            currentQueue.forEach { task ->
                if (task.status == DownloadStatus.IN_PROGRESS || 
                    task.status == DownloadStatus.QUEUED ||
                    task.status == DownloadStatus.PAUSED) {
                    downloadService.cancelDownload(task.id)
                }
            }
        }
    }
}