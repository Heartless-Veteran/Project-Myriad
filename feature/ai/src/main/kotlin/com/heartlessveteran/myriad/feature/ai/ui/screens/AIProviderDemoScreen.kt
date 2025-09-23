package com.heartlessveteran.myriad.feature.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.heartlessveteran.myriad.core.domain.ai.AIProvider
import com.heartlessveteran.myriad.core.data.ai.GeminiProvider
import com.heartlessveteran.myriad.core.data.ai.OpenAIProvider
import com.heartlessveteran.myriad.feature.ai.ui.components.AIProviderSelector
import com.heartlessveteran.myriad.feature.ai.viewmodel.AIOperationState
import com.heartlessveteran.myriad.feature.ai.viewmodel.AIViewModel

/**
 * Demo screen for AI Provider selection and functionality.
 * Shows how users can choose different AI models and test their capabilities.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIProviderDemoScreen(
    availableProviders: List<AIProvider>,
    currentProvider: AIProvider,
    aiOperationState: AIOperationState,
    onProviderSelected: (AIProvider) -> Unit,
    onGenerateRecommendations: () -> Unit,
    onAnalyzeArtStyle: () -> Unit,
    onTranslateText: () -> Unit,
    onClearState: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AI Provider Demo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Select an AI provider and test its capabilities:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "AI Provider Selection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                AIProviderSelector(
                    providers = availableProviders,
                    selectedProvider = currentProvider,
                    onProviderSelected = onProviderSelected
                )

                Text(
                    text = "Current Provider: ${currentProvider.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Test AI Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onGenerateRecommendations,
                        enabled = aiOperationState !is AIOperationState.Loading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Recommendations")
                    }

                    Button(
                        onClick = onAnalyzeArtStyle,
                        enabled = aiOperationState !is AIOperationState.Loading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Art Analysis")
                    }
                }

                Button(
                    onClick = onTranslateText,
                    enabled = aiOperationState !is AIOperationState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OCR Translation")
                }

                if (aiOperationState !is AIOperationState.Idle) {
                    Button(
                        onClick = onClearState,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Results")
                    }
                }
            }
        }

        Card(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                when (aiOperationState) {
                    is AIOperationState.Idle -> {
                        Text(
                            text = "No operation running. Select a feature to test.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is AIOperationState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "Processing with ${currentProvider.name}...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    is AIOperationState.Success -> {
                        LazyColumn {
                            when (val data = aiOperationState.data) {
                                is List<*> -> {
                                    // Check if all elements are String
                                    val allStrings = data.all { it is String }
                                    if (allStrings) {
                                        @Suppress("UNCHECKED_CAST")
                                        val stringList = data as List<String>
                                        items(stringList) { item ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                )
                                            ) {
                                                Text(
                                                    text = item,
                                                    modifier = Modifier.padding(12.dp),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    } else {
                                        item {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                                )
                                            ) {
                                                Text(
                                                    text = "Error: Received a list with non-string elements.",
                                                    modifier = Modifier.padding(12.dp),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                    }
                                }
                                is String -> {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            Text(
                                                text = data,
                                                modifier = Modifier.padding(12.dp),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer
                                            )
                                        ) {
                                            Text(
                                                text = "Error: Unexpected result type: ${data?.let { it::class.simpleName } ?: "null"}",
                                                modifier = Modifier.padding(12.dp),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is AIOperationState.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = "Error: ${aiOperationState.message}",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AIProviderDemoScreenPreview() {
    val providers = listOf(
        GeminiProvider(),
        OpenAIProvider()
    )
    
    MaterialTheme {
        AIProviderDemoScreen(
            availableProviders = providers,
            currentProvider = providers.first(),
            aiOperationState = AIOperationState.Idle,
            onProviderSelected = {},
            onGenerateRecommendations = {},
            onAnalyzeArtStyle = {},
            onTranslateText = {},
            onClearState = {}
        )
    }
}