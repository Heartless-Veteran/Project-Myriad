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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heartlessveteran.myriad.data.services.BackupServiceImpl
import com.heartlessveteran.myriad.domain.services.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Backup & Restore Management Screen for data backup and recovery.
 * 
 * Allows users to:
 * - Create manual backups
 * - View existing backup files
 * - Restore from backup files
 * - Configure automatic backup settings
 * - Manage backup storage
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val backupService = remember { BackupServiceImpl(context) }
    val scope = rememberCoroutineScope()
    
    var backups by remember { mutableStateOf<List<BackupMetadata>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCreatingBackup by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var selectedBackup by remember { mutableStateOf<BackupMetadata?>(null) }
    var backupConfiguration by remember { mutableStateOf(BackupConfiguration()) }
    
    // Load backups and configuration on screen start
    LaunchedEffect(Unit) {
        try {
            backupConfiguration = backupService.getBackupConfiguration()
            val backupsResult = backupService.getLocalBackups()
            when (backupsResult) {
                is com.heartlessveteran.myriad.domain.models.Result.Success -> {
                    backups = backupsResult.data
                }
                is com.heartlessveteran.myriad.domain.models.Result.Error -> {
                    errorMessage = "Failed to load backups: ${backupsResult.message}"
                }
                else -> {}
            }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load backup data: ${e.message}"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showBackupDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Backup"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    val backupsResult = backupService.getLocalBackups()
                                    when (backupsResult) {
                                        is com.heartlessveteran.myriad.domain.models.Result.Success -> {
                                            backups = backupsResult.data
                                        }
                                        is com.heartlessveteran.myriad.domain.models.Result.Error -> {
                                            errorMessage = "Failed to load backups: ${backupsResult.message}"
                                        }
                                        else -> {}
                                    }
                                    isLoading = false
                                } catch (e: Exception) {
                                    errorMessage = "Failed to load backup data: ${e.message}"
                                    isLoading = false
                                }
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Quick Actions Section
                        item {
                            QuickActionsSection(
                                isCreatingBackup = isCreatingBackup,
                                onCreateBackup = {
                                    scope.launch {
                                        isCreatingBackup = true
                                        try {
                                            val result = backupService.createBackup()
                                            when (result) {
                                                is com.heartlessveteran.myriad.domain.models.Result.Success -> {
                                                    val backupsResult = backupService.getLocalBackups()
                                                    if (backupsResult is com.heartlessveteran.myriad.domain.models.Result.Success) {
                                                        backups = backupsResult.data
                                                    }
                                                }
                                                is com.heartlessveteran.myriad.domain.models.Result.Error -> {
                                                    errorMessage = "Backup failed: ${result.message}"
                                                }
                                                else -> {}
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Backup failed: ${e.message}"
                                        } finally {
                                            isCreatingBackup = false
                                        }
                                    }
                                },
                                onImportBackup = {
                                    // TODO: Implement file picker for import
                                    showRestoreDialog = true
                                }
                            )
                        }
                        
                        // Configuration Section
                        item {
                            ConfigurationSection(
                                configuration = backupConfiguration,
                                onConfigurationChange = { newConfig ->
                                    scope.launch {
                                        try {
                                            backupService.updateConfiguration(newConfig)
                                            backupConfiguration = newConfig
                                        } catch (e: Exception) {
                                            errorMessage = "Failed to update configuration: ${e.message}"
                                        }
                                    }
                                }
                            )
                        }
                        
                        // Backup Files Section
                        item {
                            Text(
                                text = "Backup Files",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        if (backups.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FolderOff,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "No backup files found",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Create your first backup to secure your data",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(backups) { backup ->
                                BackupFileCard(
                                    backup = backup,
                                    onRestore = {
                                        selectedBackup = backup
                                        showRestoreDialog = true
                                    },
                                    onDelete = { backupToDelete ->
                                        scope.launch {
                                            try {
                                                backupService.deleteBackup(backupToDelete.id)
                                                val backupsResult = backupService.getLocalBackups()
                                                if (backupsResult is com.heartlessveteran.myriad.domain.models.Result.Success) {
                                                    backups = backupsResult.data
                                                }
                                            } catch (e: Exception) {
                                                errorMessage = "Failed to delete backup: ${e.message}"
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Backup Creation Dialog
    if (showBackupDialog) {
        BackupOptionsDialog(
            onDismiss = { showBackupDialog = false },
            onConfirm = { options ->
                showBackupDialog = false
                scope.launch {
                    isCreatingBackup = true
                    try {
                        val result = backupService.createBackup(
                            includeLibrary = options.includeLibrary,
                            includeProgress = options.includeProgress,
                            includeSettings = options.includeSettings,
                            includeCategories = options.includeCategories,
                            includeTrackingLinks = options.includeTrackingLinks
                        )
                        when (result) {
                            is com.heartlessveteran.myriad.domain.models.Result.Success -> {
                                val backupsResult = backupService.getLocalBackups()
                                if (backupsResult is com.heartlessveteran.myriad.domain.models.Result.Success) {
                                    backups = backupsResult.data
                                }
                            }
                            is com.heartlessveteran.myriad.domain.models.Result.Error -> {
                                errorMessage = "Backup failed: ${result.message}"
                            }
                            else -> {}
                        }
                    } catch (e: Exception) {
                        errorMessage = "Backup failed: ${e.message}"
                    } finally {
                        isCreatingBackup = false
                    }
                }
            }
        )
    }
    
    // Restore Dialog
    if (showRestoreDialog) {
        RestoreOptionsDialog(
            backup = selectedBackup,
            onDismiss = { 
                showRestoreDialog = false
                selectedBackup = null
            },
            onConfirm = { options ->
                showRestoreDialog = false
                selectedBackup = null
                // TODO: Implement restore functionality
                errorMessage = "Restore functionality coming soon"
            }
        )
    }
}

@Composable
private fun QuickActionsSection(
    isCreatingBackup: Boolean,
    onCreateBackup: () -> Unit,
    onImportBackup: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCreateBackup,
                    enabled = !isCreatingBackup,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isCreatingBackup) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Backup")
                }
                
                OutlinedButton(
                    onClick = onImportBackup,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import")
                }
            }
        }
    }
}

@Composable
private fun ConfigurationSection(
    configuration: BackupConfiguration,
    onConfigurationChange: (BackupConfiguration) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Auto Backup Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Auto Backup",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Automatically create backups",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = configuration.autoBackupEnabled,
                    onCheckedChange = { enabled ->
                        onConfigurationChange(configuration.copy(autoBackupEnabled = enabled))
                    }
                )
            }
            
            if (configuration.autoBackupEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Backup Frequency: ${configuration.autoBackupFrequency.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Max Backup Files: ${configuration.maxBackupFiles}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun BackupFileCard(
    backup: BackupMetadata,
    onRestore: () -> Unit,
    onDelete: (BackupMetadata) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = backup.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = dateFormat.format(Date(backup.createdAt)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "${formatFileSize(backup.fileSize)} • ${backup.itemCounts.mangaCount} manga",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRestore
                    ) {
                        Text("Restore")
                    }
                    
                    IconButton(
                        onClick = { onDelete(backup) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupOptionsDialog(
    onDismiss: () -> Unit,
    onConfirm: (BackupOptions) -> Unit
) {
    var includeLibrary by remember { mutableStateOf(true) }
    var includeProgress by remember { mutableStateOf(true) }
    var includeSettings by remember { mutableStateOf(true) }
    var includeCategories by remember { mutableStateOf(true) }
    var includeTrackingLinks by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Backup") },
        text = {
            Column {
                Text(
                    text = "Select what to include in the backup:",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                CheckboxOption(
                    text = "Library (manga/anime)",
                    checked = includeLibrary,
                    onCheckedChange = { includeLibrary = it }
                )
                
                CheckboxOption(
                    text = "Reading progress",
                    checked = includeProgress,
                    onCheckedChange = { includeProgress = it }
                )
                
                CheckboxOption(
                    text = "App settings",
                    checked = includeSettings,
                    onCheckedChange = { includeSettings = it }
                )
                
                CheckboxOption(
                    text = "Categories",
                    checked = includeCategories,
                    onCheckedChange = { includeCategories = it }
                )
                
                CheckboxOption(
                    text = "Tracking links",
                    checked = includeTrackingLinks,
                    onCheckedChange = { includeTrackingLinks = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        BackupOptions(
                            includeLibrary = includeLibrary,
                            includeProgress = includeProgress,
                            includeSettings = includeSettings,
                            includeCategories = includeCategories,
                            includeTrackingLinks = includeTrackingLinks
                        )
                    )
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RestoreOptionsDialog(
    backup: BackupMetadata?,
    onDismiss: () -> Unit,
    onConfirm: (RestoreOptions) -> Unit
) {
    var restoreLibrary by remember { mutableStateOf(true) }
    var restoreProgress by remember { mutableStateOf(true) }
    var restoreSettings by remember { mutableStateOf(true) }
    var restoreCategories by remember { mutableStateOf(true) }
    var restoreTrackingLinks by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore from Backup") },
        text = {
            Column {
                backup?.let {
                    Text(
                        text = "Restore from: ${it.fileName}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                Text(
                    text = "Select what to restore:",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                CheckboxOption(
                    text = "Library (manga/anime)",
                    checked = restoreLibrary,
                    onCheckedChange = { restoreLibrary = it }
                )
                
                CheckboxOption(
                    text = "Reading progress",
                    checked = restoreProgress,
                    onCheckedChange = { restoreProgress = it }
                )
                
                CheckboxOption(
                    text = "App settings",
                    checked = restoreSettings,
                    onCheckedChange = { restoreSettings = it }
                )
                
                CheckboxOption(
                    text = "Categories",
                    checked = restoreCategories,
                    onCheckedChange = { restoreCategories = it }
                )
                
                CheckboxOption(
                    text = "Tracking links",
                    checked = restoreTrackingLinks,
                    onCheckedChange = { restoreTrackingLinks = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        RestoreOptions(
                            restoreLibrary = restoreLibrary,
                            restoreProgress = restoreProgress,
                            restoreSettings = restoreSettings,
                            restoreCategories = restoreCategories,
                            restoreTrackingLinks = restoreTrackingLinks
                        )
                    )
                }
            ) {
                Text("Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CheckboxOption(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = 1024
    val mb = kb * 1024
    val gb = mb * 1024
    
    return when {
        bytes >= gb -> String.format("%.1f GB", bytes.toDouble() / gb)
        bytes >= mb -> String.format("%.1f MB", bytes.toDouble() / mb)
        bytes >= kb -> String.format("%.1f KB", bytes.toDouble() / kb)
        else -> "$bytes B"
    }
}