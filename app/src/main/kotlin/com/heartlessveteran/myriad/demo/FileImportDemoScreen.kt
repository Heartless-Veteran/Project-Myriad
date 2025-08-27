package com.heartlessveteran.myriad.demo

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heartlessveteran.myriad.ui.components.FileImportDialog
import com.heartlessveteran.myriad.ui.components.ImportProgressDialog
import com.heartlessveteran.myriad.ui.viewmodel.FileImportViewModel

/**
 * Demo screen showing file import functionality integration.
 * 
 * This demonstrates how to:
 * - Integrate FileImportDialog for file selection
 * - Use FileImportViewModel for import operations  
 * - Show import progress and results
 * - Handle both single file and directory imports
 */
@Composable
fun FileImportDemoScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = remember { FileImportViewModel(context) }
    
    val importStatus by viewModel.importStatus.collectAsState()
    val importedManga by viewModel.importedManga.collectAsState()
    
    var showImportDialog by remember { mutableStateOf(false) }
    var showProgressDialog by remember { mutableStateOf(false) }
    
    // Show progress dialog when import is running
    LaunchedEffect(importStatus) {
        showProgressDialog = importStatus.isLoading || importStatus.isError || importStatus.isSuccess
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "File Import Demo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Demonstration of the File Management System integration",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Import button
        Button(
            onClick = { showImportDialog = true },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(
                imageVector = Icons.Default.GetApp,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import Manga Files")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Import status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Import Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                when {
                    importStatus.isLoading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Text(importStatus.message)
                        }
                    }
                    importStatus.isSuccess -> {
                        Text(
                            text = importStatus.message,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    importStatus.isError -> {
                        Text(
                            text = importStatus.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {
                        Text(
                            text = "No import operations yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Imported manga list
        if (importedManga.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Imported Manga (${importedManga.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    importedManga.forEach { manga ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = manga.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = manga.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Author: ${manga.author}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    if (importedManga.size > 3) {
                        TextButton(
                            onClick = { viewModel.clearImportedManga() }
                        ) {
                            Text("Clear List")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Implementation notes
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎯 Implementation Complete",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "• Full .cbz/.cbr archive extraction with zip4j\n" +
                          "• Intelligent metadata parsing from filenames\n" + 
                          "• Natural page sorting and validation\n" +
                          "• Directory scanning with recursive support\n" +
                          "• Comprehensive error handling and progress tracking",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
    
    // File import dialog
    FileImportDialog(
        isVisible = showImportDialog,
        onDismiss = { showImportDialog = false },
        onFileSelected = { uri: Uri -> 
            viewModel.importFile(uri)
        },
        onDirectorySelected = { uri: Uri -> 
            viewModel.importDirectory(uri)  
        }
    )
    
    // Progress dialog
    ImportProgressDialog(
        isVisible = showProgressDialog,
        onDismiss = { 
            showProgressDialog = false
            viewModel.clearImportStatus()
        },
        importStatus = importStatus
    )
}