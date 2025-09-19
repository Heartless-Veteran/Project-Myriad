package com.heartlessveteran.myriad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.heartlessveteran.myriad.domain.services.DownloadStatus
import com.heartlessveteran.myriad.domain.services.DownloadTask
import com.heartlessveteran.myriad.ui.theme.MyriadTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * Download queue management screen.
 *
 * This screen provides:
 * - View of all active and completed downloads
 * - Download progress tracking with visual indicators
 * - Download management actions (pause/resume/cancel/retry)
 * - Overall download statistics and controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadQueueScreen(
    downloadQueue: List<DownloadTask>,
    overallProgress: Float,
    areDownloadsActive: Boolean,
    onPauseDownload: (String) -> Unit,
    onResumeDownload: (String) -> Unit,
    onCancelDownload: (String) -> Unit,
    onRetryDownload: (String) -> Unit,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        // Header section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Download Queue",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Overall progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Overall Progress",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "${(overallProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { overallProgress },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Queue statistics
                val activeTasks =
                    downloadQueue.filter {
                        it.status == DownloadStatus.IN_PROGRESS || it.status == DownloadStatus.QUEUED
                    }
                val completedTasks = downloadQueue.filter { it.status == DownloadStatus.COMPLETED }
                val failedTasks = downloadQueue.filter { it.status == DownloadStatus.FAILED }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    StatisticChip(
                        label = "Active",
                        value = activeTasks.size.toString(),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    StatisticChip(
                        label = "Completed",
                        value = completedTasks.size.toString(),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    StatisticChip(
                        label = "Failed",
                        value = failedTasks.size.toString(),
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (completedTasks.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onClearCompleted,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Clear Completed")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Download list
        if (downloadQueue.isEmpty()) {
            // Empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "No downloads in queue",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start downloading manga to see them here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(downloadQueue) { task ->
                    DownloadTaskCard(
                        task = task,
                        onPauseDownload = onPauseDownload,
                        onResumeDownload = onResumeDownload,
                        onCancelDownload = onCancelDownload,
                        onRetryDownload = onRetryDownload,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticChip(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadTaskCard(
    task: DownloadTask,
    onPauseDownload: (String) -> Unit,
    onResumeDownload: (String) -> Unit,
    onCancelDownload: (String) -> Unit,
    onRetryDownload: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // Title and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = task.mangaTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${task.chapterIds.size} chapters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status chip
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = getStatusColor(task.status).copy(alpha = 0.2f),
                ) {
                    Text(
                        text = task.status.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = getStatusColor(task.status),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress section
            when (task.status) {
                DownloadStatus.IN_PROGRESS, DownloadStatus.QUEUED -> {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = if (task.status == DownloadStatus.QUEUED) "Waiting..." else "Downloading...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${(task.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { if (task.status == DownloadStatus.QUEUED) 0f else task.progress },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (task.totalBytes > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatFileSize(task.downloadedBytes, task.totalBytes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                DownloadStatus.FAILED -> {
                    task.errorMessage?.let { error ->
                        Text(
                            text = "Error: $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                DownloadStatus.COMPLETED -> {
                    Text(
                        text = "Completed ${formatTimestamp(task.completedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> {
                    // PAUSED, CANCELLED
                    Text(
                        text = task.status.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (task.status) {
                    DownloadStatus.IN_PROGRESS -> {
                        IconButton(
                            onClick = { onPauseDownload(task.id) },
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause")
                        }
                    }
                    DownloadStatus.PAUSED, DownloadStatus.QUEUED -> {
                        IconButton(
                            onClick = { onResumeDownload(task.id) },
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                        }
                    }
                    DownloadStatus.FAILED -> {
                        IconButton(
                            onClick = { onRetryDownload(task.id) },
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry")
                        }
                    }
                    else -> {
                        // No primary action for completed/cancelled
                    }
                }

                // Cancel button (available for most states)
                if (task.status != DownloadStatus.COMPLETED && task.status != DownloadStatus.CANCELLED) {
                    IconButton(
                        onClick = { onCancelDownload(task.id) },
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getStatusColor(status: DownloadStatus): androidx.compose.ui.graphics.Color =
    when (status) {
        DownloadStatus.QUEUED -> MaterialTheme.colorScheme.primary
        DownloadStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        DownloadStatus.PAUSED -> MaterialTheme.colorScheme.outline
        DownloadStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
        DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
        DownloadStatus.CANCELLED -> MaterialTheme.colorScheme.outlineVariant
    }

private fun formatFileSize(
    downloaded: Long,
    total: Long,
): String {
    val downloadedStr =
        when {
            downloaded >= 1024 * 1024 * 1024 -> "%.1f GB".format(downloaded / (1024.0 * 1024.0 * 1024.0))
            downloaded >= 1024 * 1024 -> "%.1f MB".format(downloaded / (1024.0 * 1024.0))
            downloaded >= 1024 -> "%.1f KB".format(downloaded / 1024.0)
            else -> "$downloaded B"
        }

    val totalStr =
        when {
            total >= 1024 * 1024 * 1024 -> "%.1f GB".format(total / (1024.0 * 1024.0 * 1024.0))
            total >= 1024 * 1024 -> "%.1f MB".format(total / (1024.0 * 1024.0))
            total >= 1024 -> "%.1f KB".format(total / 1024.0)
            else -> "$total B"
        }

    return "$downloadedStr / $totalStr"
}

private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) return ""
    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
private fun DownloadQueueScreenPreview() {
    MyriadTheme {
        val sampleTasks =
            listOf(
                DownloadTask(
                    id = "1",
                    mangaId = "manga1",
                    mangaTitle = "One Piece",
                    chapterIds = listOf("ch1", "ch2", "ch3"),
                    status = DownloadStatus.IN_PROGRESS,
                    progress = 0.65f,
                    downloadedBytes = 45 * 1024 * 1024,
                    totalBytes = 70 * 1024 * 1024,
                ),
                DownloadTask(
                    id = "2",
                    mangaId = "manga2",
                    mangaTitle = "Attack on Titan",
                    chapterIds = listOf("ch1"),
                    status = DownloadStatus.COMPLETED,
                    progress = 1.0f,
                    completedAt = System.currentTimeMillis() - 3600000,
                ),
                DownloadTask(
                    id = "3",
                    mangaId = "manga3",
                    mangaTitle = "Demon Slayer",
                    chapterIds = listOf("ch1", "ch2"),
                    status = DownloadStatus.FAILED,
                    errorMessage = "Network connection failed",
                ),
            )

        DownloadQueueScreen(
            downloadQueue = sampleTasks,
            overallProgress = 0.4f,
            areDownloadsActive = true,
            onPauseDownload = {},
            onResumeDownload = {},
            onCancelDownload = {},
            onRetryDownload = {},
            onClearCompleted = {},
        )
    }
}
