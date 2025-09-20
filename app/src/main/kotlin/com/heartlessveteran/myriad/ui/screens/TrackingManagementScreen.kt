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
import com.heartlessveteran.myriad.data.services.TrackingServiceImpl
import com.heartlessveteran.myriad.domain.services.TrackingServiceProvider
import kotlinx.coroutines.launch

/**
 * Tracking Services Management Screen for configuring external tracking.
 * 
 * Allows users to:
 * - View available tracking services (MyAnimeList, AniList)
 * - Authenticate with tracking services
 * - Manage tracking preferences
 * - View authentication status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingManagementScreen(
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val trackingService = remember { TrackingServiceImpl(context) }
    val scope = rememberCoroutineScope()
    
    var availableServices by remember { mutableStateOf<List<TrackingServiceProvider>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Load tracking services on screen start
    LaunchedEffect(Unit) {
        try {
            availableServices = trackingService.getAvailableServices()
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load tracking services: ${e.message}"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tracking Services") },
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
                                    availableServices = trackingService.getAvailableServices()
                                    isLoading = false
                                } catch (e: Exception) {
                                    errorMessage = "Failed to load tracking services: ${e.message}"
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
                        text = "Progress Tracking",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "Connect your accounts to sync reading and watching progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(availableServices) { service ->
                            TrackingServiceCard(
                                service = service,
                                onConnect = { serviceId ->
                                    scope.launch {
                                        try {
                                            val authSession = trackingService.startAuthentication(serviceId)
                                            // TODO: Open browser or handle authentication
                                            // For now just show a placeholder
                                        } catch (e: Exception) {
                                            errorMessage = "Failed to start authentication: ${e.message}"
                                        }
                                    }
                                },
                                onDisconnect = { serviceId ->
                                    scope.launch {
                                        try {
                                            trackingService.disconnect(serviceId)
                                            availableServices = trackingService.getAvailableServices()
                                        } catch (e: Exception) {
                                            errorMessage = "Failed to disconnect: ${e.message}"
                                        }
                                    }
                                }
                            )
                        }
                        
                        // Add informational card
                        item {
                            InfoCard()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackingServiceCard(
    service: TrackingServiceProvider,
    onConnect: (String) -> Unit,
    onDisconnect: (String) -> Unit
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
                        text = service.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = service.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    // Authentication status
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (service.isAuthenticated) 
                                Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (service.isAuthenticated) 
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Text(
                            text = if (service.isAuthenticated) "Connected" else "Not connected",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (service.isAuthenticated) 
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (service.isAuthenticated) {
                        OutlinedButton(
                            onClick = { onDisconnect(service.id) }
                        ) {
                            Text("Disconnect")
                        }
                    } else {
                        Button(
                            onClick = { onConnect(service.id) }
                        ) {
                            Text("Connect")
                        }
                    }
                }
            }
            
            // Show supported types and features
            if (service.supportedTypes.isNotEmpty() || service.features.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                if (service.supportedTypes.isNotEmpty()) {
                    Text(
                        text = "Supports:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        service.supportedTypes.forEach { type ->
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }
                }
                
                if (service.features.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Features:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        service.features.take(3).forEach { feature ->
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
                        
                        if (service.features.size > 3) {
                            Text(
                                text = "+${service.features.size - 3} more",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "About Progress Tracking",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "• Automatically sync your reading and watching progress\n" +
                      "• Keep your lists updated across devices\n" +
                      "• Share your achievements with the community\n" +
                      "• Access your lists from anywhere",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatFeatureName(feature: com.heartlessveteran.myriad.domain.services.TrackingFeature): String {
    return when (feature) {
        com.heartlessveteran.myriad.domain.services.TrackingFeature.OAUTH2_AUTH -> "OAuth2"
        com.heartlessveteran.myriad.domain.services.TrackingFeature.PROGRESS_SYNC -> "Progress Sync"
        com.heartlessveteran.myriad.domain.services.TrackingFeature.SCORE_SYNC -> "Score Sync"
        com.heartlessveteran.myriad.domain.services.TrackingFeature.STATUS_SYNC -> "Status Sync"
        com.heartlessveteran.myriad.domain.services.TrackingFeature.LIST_IMPORT -> "List Import"
        com.heartlessveteran.myriad.domain.services.TrackingFeature.LIST_EXPORT -> "List Export"
        com.heartlessveteran.myriad.domain.services.TrackingFeature.BULK_SYNC -> "Bulk Sync"
        com.heartlessveteran.myriad.domain.services.TrackingFeature.REAL_TIME_SYNC -> "Real-time"
    }
}