package com.heartlessveteran.myriad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heartlessveteran.myriad.di.AppDiContainer
import com.heartlessveteran.myriad.domain.services.ContentSource
import com.heartlessveteran.myriad.domain.services.SourceFeature
import kotlinx.coroutines.launch

/**
 * Source Management Screen for configuring online content sources.
 * 
 * Allows users to:
 * - View available sources (MangaDx, Komikku, etc.)
 * - Enable/disable sources
 * - View source capabilities and status
 * - Configure source-specific settings (future)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceManagementScreen(
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val sourceService = remember { AppDiContainer.getSourceService() }
    val scope = rememberCoroutineScope()
    
    var availableSources by remember { mutableStateOf<List<ContentSource>>(emptyList()) }
    var enabledSources by remember { mutableStateOf<List<ContentSource>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load sources on screen start
    LaunchedEffect(Unit) {
        try {
            availableSources = sourceService.getAvailableSources()
            enabledSources = sourceService.getEnabledSources()
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load sources: ${e.message}"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Source Management") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
                                    availableSources = sourceService.getAvailableSources()
                                    enabledSources = sourceService.getEnabledSources()
                                    isLoading = false
                                } catch (e: Exception) {
                                    errorMessage = "Failed to load sources: ${e.message}"
                                    isLoading = false
                                }
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    Text(
                        text = "Content Sources",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "Manage online sources for manga discovery",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(availableSources) { source ->
                            SourceCard(
                                source = source,
                                isEnabled = enabledSources.any { it.id == source.id },
                                onToggleEnabled = { enabled ->
                                    scope.launch {
                                        try {
                                            sourceService.setSourceEnabled(source.id, enabled)
                                            enabledSources = sourceService.getEnabledSources()
                                        } catch (e: Exception) {
                                            errorMessage = "Failed to update source: ${e.message}"
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

@Composable
private fun SourceCard(
    source: ContentSource,
    isEnabled: Boolean,
    onToggleEnabled: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = source.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = source.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "v${source.version}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        if (source.isOfficial) {
                            Text(
                                text = "Official",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = onToggleEnabled
                    )
                    
                    if (source.hasSettings) {
                        IconButton(
                            onClick = { /* TODO: Open source settings */ },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            // Show supported features
            if (source.supportedFeatures.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Features:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    source.supportedFeatures.take(3).forEach { feature ->
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = formatFeatureName(feature),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                    
                    if (source.supportedFeatures.size > 3) {
                        Text(
                            text = "+${source.supportedFeatures.size - 3} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatFeatureName(feature: SourceFeature): String {
    return when (feature) {
        SourceFeature.SEARCH -> "Search"
        SourceFeature.LATEST -> "Latest"
        SourceFeature.POPULAR -> "Popular"
        SourceFeature.DETAILS -> "Details"
        SourceFeature.CHAPTERS -> "Chapters"
        SourceFeature.FILTERING -> "Filters"
        SourceFeature.LOGIN_REQUIRED -> "Login"
        SourceFeature.RATE_LIMITED -> "Rate Limited"
        SourceFeature.NSFW_CONTENT -> "NSFW"
    }
}