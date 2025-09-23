package com.heartlessveteran.myriad.feature.vault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.heartlessveteran.myriad.feature.vault.data.download.DownloadItem
import com.heartlessveteran.myriad.feature.vault.data.download.DownloadProgress
import com.heartlessveteran.myriad.feature.vault.data.download.DownloadStatus
import com.heartlessveteran.myriad.feature.vault.data.download.DownloadType

/**
 * Screen for managing downloads queue and progress.
 * Shows active downloads, completed downloads, and allows download control.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadManagerScreen(
    downloads: List<DownloadItem> = emptyList(),
    downloadProgress: Map<String, DownloadProgress> = emptyMap(),
    onPauseDownload: (String) -> Unit = {},
    onResumeDownload: (String) -> Unit = {},
    onCancelDownload: (String) -> Unit = {},
    onCancelAllDownloads: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Sample data for preview
    val sampleDownloads = remember {
        if (downloads.isNotEmpty()) downloads else listOf(
            DownloadItem(
                id = "1",
                title = "One Piece Chapter 1000",
                url = "https://example.com/chapter1000.cbz",
                type = DownloadType.MANGA_CHAPTER
            ),
            DownloadItem(
                id = "2", 
                title = "Attack on Titan Chapter 139",
                url = "https://example.com/chapter139.cbz",
                type = DownloadType.MANGA_CHAPTER
            ),
            DownloadItem(
                id = "3",
                title = "Demon Slayer Episode 1",
                url = "https://example.com/episode1.mp4",
                type = DownloadType.ANIME_EPISODE
            )
        )
    }

    val sampleProgress = remember {
        if (downloadProgress.isNotEmpty()) downloadProgress else mapOf(
            "1" to DownloadProgress("1", DownloadStatus.DOWNLOADING, 0.65f, 650000, 1000000),
            "2" to DownloadProgress("2", DownloadStatus.COMPLETED, 1.0f, 800000, 800000),
            "3" to DownloadProgress("3", DownloadStatus.PAUSED, 0.30f, 150000, 500000)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Download Manager")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onCancelAllDownloads) {
                        Icon(Icons.Default.Clear, contentDescription = "Cancel All")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Download statistics
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Download Queue",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val activeCount = sampleProgress.values.count { 
                            it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.QUEUED 
                        }
                        val completedCount = sampleProgress.values.count { 
                            it.status == DownloadStatus.COMPLETED 
                        }
                        
                        Text(
                            text = "$activeCount active • $completedCount completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Downloads list
            if (sampleDownloads.isNotEmpty()) {
                Text(
                    text = "Downloads",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sampleDownloads) { download ->
                        DownloadItemCard(
                            download = download,
                            progress = sampleProgress[download.id],
                            onPause = { onPauseDownload(download.id) },
                            onResume = { onResumeDownload(download.id) },
                            onCancel = { onCancelDownload(download.id) }
                        )
                    }
                }
            } else {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No downloads in queue",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Downloads will appear here when you add them",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadItemCard(
    download: DownloadItem,
    progress: DownloadProgress?,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title and type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = download.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when (download.type) {
                            DownloadType.MANGA_CHAPTER -> "Manga Chapter"
                            DownloadType.MANGA_VOLUME -> "Manga Volume"
                            DownloadType.ANIME_EPISODE -> "Anime Episode"
                            DownloadType.ANIME_SEASON -> "Anime Season"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Status badge
                if (progress != null) {
                    AssistChip(
                        onClick = { },
                        label = { 
                            Text(
                                when (progress.status) {
                                    DownloadStatus.QUEUED -> "Queued"
                                    DownloadStatus.DOWNLOADING -> "Downloading"
                                    DownloadStatus.PAUSED -> "Paused"
                                    DownloadStatus.COMPLETED -> "Completed"
                                    DownloadStatus.FAILED -> "Failed"
                                    DownloadStatus.CANCELLED -> "Cancelled"
                                }
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (progress.status) {
                                DownloadStatus.DOWNLOADING -> MaterialTheme.colorScheme.primaryContainer
                                DownloadStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                                DownloadStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    )
                }
            }
            
            // Progress bar and info
            if (progress != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = progress.progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${(progress.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = formatBytes(progress.bytesDownloaded) + " / " + 
                                    formatBytes(progress.totalBytes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (progress?.status) {
                    DownloadStatus.DOWNLOADING, DownloadStatus.QUEUED -> {
                        IconButton(onClick = onPause) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause")
                        }
                    }
                    DownloadStatus.PAUSED, DownloadStatus.FAILED -> {
                        IconButton(onClick = onResume) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                        }
                    }
                    else -> {
                        // No action buttons for completed/cancelled
                    }
                }
                
                if (progress?.status != DownloadStatus.COMPLETED) {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        bytes >= 1024 -> "${bytes / 1024} KB"
        else -> "$bytes B"
    }
}

@Preview(showBackground = true)
@Composable
private fun DownloadManagerScreenPreview() {
    MaterialTheme {
        DownloadManagerScreen()
    }
}