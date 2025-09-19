package com.heartlessveteran.myriad.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

/**
 * File import dialog for selecting local manga files.
 *
 * Provides options to:
 * - Import individual .cbz/.cbr files
 * - Import from a directory
 * - Show import progress
 */
@Composable
fun FileImportDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onFileSelected: (Uri) -> Unit,
    onDirectorySelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // File picker launcher
    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri: Uri? ->
            uri?.let { onFileSelected(it) }
        }

    // Directory picker launcher
    val directoryPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree(),
        ) { uri: Uri? ->
            uri?.let { onDirectorySelected(it) }
        }

    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties =
                DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    usePlatformDefaultWidth = false,
                ),
        ) {
            Card(
                modifier =
                    modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Dialog Title
                    Text(
                        text = "Import Manga",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = "Choose how to import your local manga files:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Import single file option
                    ImportOptionCard(
                        icon = Icons.Default.FileOpen,
                        title = "Import File",
                        description = "Select individual .cbz or .cbr files",
                        supportedFormats = "Supports: .cbz, .cbr, .zip",
                        onClick = {
                            coroutineScope.launch {
                                filePickerLauncher.launch(
                                    arrayOf(
                                        "application/zip",
                                        "application/x-cbz",
                                        "application/x-cbr",
                                        "application/x-rar-compressed",
                                    ),
                                )
                            }
                            onDismiss()
                        },
                    )

                    // Import directory option
                    ImportOptionCard(
                        icon = Icons.Default.FolderOpen,
                        title = "Import Directory",
                        description = "Scan a folder for manga files",
                        supportedFormats = "Recursively scans subdirectories",
                        onClick = {
                            coroutineScope.launch {
                                directoryPickerLauncher.launch(null)
                            }
                            onDismiss()
                        },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dialog actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    supportedFormats: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = supportedFormats,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

/**
 * Import progress dialog showing file import status.
 */
@Composable
fun ImportProgressDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    importStatus: ImportStatus,
    modifier: Modifier = Modifier,
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = { if (importStatus.isComplete) onDismiss() },
            properties =
                DialogProperties(
                    dismissOnBackPress = importStatus.isComplete,
                    dismissOnClickOutside = importStatus.isComplete,
                ),
        ) {
            Card(
                modifier =
                    modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Importing Manga",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )

                    when {
                        importStatus.isLoading -> {
                            CircularProgressIndicator()
                            Text(
                                text = importStatus.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        importStatus.isError -> {
                            Text(
                                text = "Import Failed",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Text(
                                text = importStatus.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Button(onClick = onDismiss) {
                                Text("Close")
                            }
                        }
                        importStatus.isSuccess -> {
                            Text(
                                text = "Import Successful!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = importStatus.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Button(onClick = onDismiss) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Data class representing import operation status.
 */
data class ImportStatus(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val message: String = "",
) {
    val isComplete: Boolean get() = isSuccess || isError
}
