package com.heartlessveteran.myriad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heartlessveteran.myriad.di.AppDiContainer
import com.heartlessveteran.myriad.domain.ai.AIFeature
import com.heartlessveteran.myriad.domain.vault.VaultStatistics
import com.heartlessveteran.myriad.app.extensions.ExtensionConfiguration

/**
 * Epic Demo Screen showing the unified next-gen manga reader core features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpicDemoScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: EpicDemoViewModel = viewModel { EpicDemoViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Epic: Next-Gen Core Demo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "🚀 Unified Next-Gen Manga Reader Core",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Complete architectural foundation with Extension System, Vault, AI Core, and Community Compatibility",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                ExtensionSystemCard(
                    configuration = uiState.extensionConfiguration,
                    onInitialize = viewModel::initializeExtensions
                )
            }

            item {
                VaultSystemCard(
                    statistics = uiState.vaultStatistics,
                    onInitialize = viewModel::initializeVault
                )
            }

            item {
                AICoreCard(
                    features = uiState.aiFeatures,
                    onInitialize = viewModel::initializeAICore
                )
            }

            item {
                ArchitectureOverviewCard()
            }
        }
    }
}

@Composable
private fun ExtensionSystemCard(
    configuration: ExtensionConfiguration?,
    onInitialize: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Extension, contentDescription = null)
                Text(
                    text = "Extension & Source System",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "Three-layer extensible architecture for online discovery",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (configuration != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoChip("Total Sources", configuration.totalSources.toString())
                    InfoChip("Built-in", configuration.builtInSources.toString())
                    InfoChip("External", configuration.externalSources.toString())
                }
                Text(
                    text = "✓ Domain Layer: Pure Kotlin Source interface\n" +
                          "✓ Data Layer: TachiyomiHttpSource compatibility\n" +
                          "✓ App Layer: ExtensionManager with PathClassLoader",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(
                    onClick = onInitialize,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Initialize Extension System")
                }
            }
        }
    }
}

@Composable
private fun VaultSystemCard(
    statistics: VaultStatistics?,
    onInitialize: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Storage, contentDescription = null)
                Text(
                    text = "Vault - Local Media Engine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "Offline-first content management (.cbz/.cbr, .mp4/.mkv/.avi)",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (statistics != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoChip("Items", statistics.totalItems.toString())
                    InfoChip("Collections", statistics.totalCollections.toString())
                    InfoChip("Tags", statistics.totalTags.toString())
                }
                Text(
                    text = "✓ Multi-format support: Manga & Anime\n" +
                          "✓ Smart organization with collections & tags\n" +
                          "✓ File system abstraction & metadata extraction",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(
                    onClick = onInitialize,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Initialize Vault System")
                }
            }
        }
    }
}

@Composable
private fun AICoreCard(
    features: List<AIFeature>,
    onInitialize: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = null)
                Text(
                    text = "AI Core - Intelligent Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "Modular AI capabilities for enhanced manga/anime experience",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (features.isNotEmpty()) {
                val availableFeatures = features.count { it.isAvailable }
                val enabledFeatures = features.count { it.isEnabled }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoChip("Features", features.size.toString())
                    InfoChip("Available", availableFeatures.toString())
                    InfoChip("Enabled", enabledFeatures.toString())
                }
                
                Text(
                    text = "✓ OCR Translation: Real-time text extraction\n" +
                          "✓ Art Style Analysis: Computer vision categorization\n" +
                          "✓ AI Recommendations: Behavior-based suggestions\n" +
                          "✓ NLP Search: Natural language query parsing",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(
                    onClick = onInitialize,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Initialize AI Core")
                }
            }
        }
    }
}

@Composable
private fun ArchitectureOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🏗️ Clean Architecture Implementation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = """
                App Layer → ExtensionManager, UI/VM, Navigation
                Domain Layer → Source interface, VaultService, AI Core  
                Data Layer → TachiyomiHttpSource, VaultFileSync, AI Providers
                
                ✅ Type Safety: 100% Kotlin with null safety
                ✅ Result Wrappers: Comprehensive error handling
                ✅ MVVM Pattern: Clean separation of concerns
                ✅ Community Ready: Tachiyomi-compatible base classes
                """.trimIndent(),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    AssistChip(
        onClick = { },
        label = { 
            Text(
                text = "$label: $value",
                style = MaterialTheme.typography.labelSmall
            ) 
        }
    )
}