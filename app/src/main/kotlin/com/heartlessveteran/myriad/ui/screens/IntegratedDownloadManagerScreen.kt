package com.heartlessveteran.myriad.ui.screens

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
import androidx.compose.ui.unit.dp

/**
 * Integrated Download Manager Screen - Phase 2 Implementation
 * Queue-based download system with intelligent bandwidth management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegratedDownloadManagerScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val downloadQueue = remember {
        listOf(
            DownloadItem(
                id = "1",
                title = "One Piece Chapter 1095",
                type = DownloadType.MANGA,
                status = DownloadStatus.DOWNLOADING,
                progress = 65f,
                size = "12.5 MB",
                speed = "2.1 MB/s"
            ),
            DownloadItem(
                id = "2", 
                title = "Attack on Titan Final Season",
                type = DownloadType.ANIME,
                status = DownloadStatus.QUEUED,
                progress = 0f,
                size = "1.2 GB",
                speed = ""
            ),
            DownloadItem(
                id = "3",
                title = "Demon Slayer Volume 15",
                type = DownloadType.MANGA,
                status = DownloadStatus.COMPLETED,
                progress = 100f,
                size = "85.3 MB",
                speed = ""
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Download Manager") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Pause all */ }) {
                        Icon(Icons.Default.PlayArrow, "Stop All")
                    }
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Download Stats
            DownloadStatsCard()
            
            // Tab Selection
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Completed") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Settings") }
                )
            }
            
            // Content based on selected tab
            when (selectedTab) {
                0 -> ActiveDownloadsTab(
                    downloads = downloadQueue.filter { 
                        it.status != DownloadStatus.COMPLETED 
                    }
                )
                1 -> CompletedDownloadsTab(
                    downloads = downloadQueue.filter { 
                        it.status == DownloadStatus.COMPLETED 
                    }
                )
                2 -> DownloadSettingsTab()
            }
        }
    }
}

@Composable
private fun DownloadStatsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Active", "2", Icons.Default.CheckCircle)
            StatItem("Queued", "5", Icons.Default.List)
            StatItem("Speed", "2.1 MB/s", Icons.Default.Star)
            StatItem("Storage", "4.2 GB", Icons.Default.Info)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActiveDownloadsTab(downloads: List<DownloadItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(downloads) { download ->
            DownloadItemCard(
                download = download,
                onPause = { /* Handle pause */ },
                onResume = { /* Handle resume */ },
                onCancel = { /* Handle cancel */ }
            )
        }
        
        if (downloads.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No active downloads",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedDownloadsTab(downloads: List<DownloadItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(downloads) { download ->
            CompletedDownloadCard(download)
        }
    }
}

@Composable
private fun DownloadSettingsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bandwidth Settings
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Bandwidth Management",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                var maxSpeed by remember { mutableFloatStateOf(5.0f) }
                Text("Max Download Speed: ${maxSpeed.toInt()} MB/s")
                Slider(
                    value = maxSpeed,
                    onValueChange = { maxSpeed = it },
                    valueRange = 1f..10f,
                    steps = 8
                )
                
                var parallelDownloads by remember { mutableFloatStateOf(3f) }
                Text("Parallel Downloads: ${parallelDownloads.toInt()}")
                Slider(
                    value = parallelDownloads,
                    onValueChange = { parallelDownloads = it },
                    valueRange = 1f..5f,
                    steps = 3
                )
            }
        }
        
        // Storage Settings
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Storage Management",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                var autoCleanup by remember { mutableStateOf(true) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto cleanup old downloads")
                    Switch(
                        checked = autoCleanup,
                        onCheckedChange = { autoCleanup = it }
                    )
                }
                
                var wifiOnly by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Download only on WiFi")
                    Switch(
                        checked = wifiOnly,
                        onCheckedChange = { wifiOnly = it }
                    )
                }
            }
        }
    }
}

data class DownloadItem(
    val id: String,
    val title: String,
    val type: DownloadType,
    val status: DownloadStatus,
    val progress: Float,
    val size: String,
    val speed: String
)

enum class DownloadType { MANGA, ANIME }
enum class DownloadStatus { DOWNLOADING, PAUSED, QUEUED, COMPLETED, ERROR }

@Composable
private fun DownloadItemCard(
    download: DownloadItem,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${download.type.name} • ${download.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    when (download.status) {
                        DownloadStatus.DOWNLOADING -> {
                            IconButton(onClick = onPause) {
                                Icon(Icons.Default.PlayArrow, "Pause")
                            }
                        }
                        DownloadStatus.PAUSED -> {
                            IconButton(onClick = onResume) {
                                Icon(Icons.Default.PlayArrow, "Resume")
                            }
                        }
                        else -> {}
                    }
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Clear, "Cancel")
                    }
                }
            }
            
            // Progress
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { download.progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${download.progress.toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (download.speed.isNotEmpty()) {
                        Text(
                            text = download.speed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedDownloadCard(download: DownloadItem) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = download.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${download.type.name} • ${download.size} • Completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { /* Open */ }) {
                    Icon(Icons.Default.Edit, "Open")
                }
                IconButton(onClick = { /* Delete */ }) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }
        }
    }
}