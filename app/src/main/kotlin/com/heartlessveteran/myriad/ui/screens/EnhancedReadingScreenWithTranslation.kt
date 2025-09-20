package com.heartlessveteran.myriad.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heartlessveteran.myriad.di.AppDiContainer
import com.heartlessveteran.myriad.ui.components.TranslationControlPanel
import com.heartlessveteran.myriad.ui.components.TranslationOverlay
import com.heartlessveteran.myriad.ui.components.TranslationSettingsPanel
import com.heartlessveteran.myriad.ui.viewmodel.TranslationViewModel

/**
 * Enhanced Reading Screen with translation overlay capabilities
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedReadingScreenWithTranslation(
    mangaId: String,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val translationViewModel: TranslationViewModel = viewModel {
        TranslationViewModel(AppDiContainer.getEnhancedAIService(context))
    }
    
    var currentPage by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(10) } // Mock data
    var isMenuVisible by remember { mutableStateOf(false) }
    var showTranslationSettings by remember { mutableStateOf(false) }
    
    val translationState by translationViewModel.translationState.collectAsState()
    val targetLanguage by translationViewModel.targetLanguage.collectAsState()
    
    Scaffold(
        topBar = {
            if (isMenuVisible) {
                TopAppBar(
                    title = { Text("Chapter 1 - Page $currentPage/$totalPages") },
                    navigationIcon = {
                        IconButton(onClick = onBackPress) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Bookmark */ }) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark")
                        }
                        IconButton(onClick = { showTranslationSettings = !showTranslationSettings }) {
                            Icon(Icons.Default.Settings, contentDescription = "Translation Settings")
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (isMenuVisible) {
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = {
                                if (currentPage > 1) {
                                    currentPage--
                                    translationViewModel.clearTranslation()
                                }
                            },
                            enabled = currentPage > 1,
                        ) {
                            Icon(Icons.Default.NavigateBefore, contentDescription = "Previous Page")
                        }

                        Text(
                            text = "$currentPage / $totalPages",
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        IconButton(
                            onClick = {
                                if (currentPage < totalPages) {
                                    currentPage++
                                    translationViewModel.clearTranslation()
                                }
                            },
                            enabled = currentPage < totalPages,
                        ) {
                            Icon(Icons.Default.NavigateNext, contentDescription = "Next Page")
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main reading area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isMenuVisible = !isMenuVisible },
                contentAlignment = Alignment.Center,
            ) {
                // Mock manga page display
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(0.7f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Mock page content
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(
                                text = "Manga: $mangaId",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Page $currentPage",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "[Mock Japanese Text: こんにちは世界]",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "Tap screen to toggle controls",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        
                        // Translation overlay
                        if (translationState.isTranslationVisible && translationState.translatedTexts.isNotEmpty()) {
                            TranslationOverlay(
                                translatedTexts = translationState.translatedTexts,
                                isVisible = true,
                                pageWidth = 350.dp, // This should match the actual page size
                                pageHeight = 500.dp,
                                onToggleTranslation = { translationViewModel.toggleTranslationVisibility() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Page progress indicator
                if (isMenuVisible) {
                    LinearProgressIndicator(
                        progress = { currentPage.toFloat() / totalPages.toFloat() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                    )
                }
            }
            
            // Translation control panel (overlay at bottom)
            if (isMenuVisible) {
                TranslationControlPanel(
                    isTranslationVisible = translationState.isTranslationVisible,
                    isTranslating = translationState.isLoading,
                    onToggleTranslation = { translationViewModel.toggleTranslationVisibility() },
                    onStartTranslation = { 
                        // Mock base64 image for testing - in real implementation this would be the actual page image
                        translationViewModel.translatePage("mock_base64_image_data", targetLanguage)
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            
            // Translation settings panel
            if (showTranslationSettings) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showTranslationSettings = false },
                    contentAlignment = Alignment.Center
                ) {
                    TranslationSettingsPanel(
                        targetLanguage = targetLanguage,
                        onLanguageChange = { language ->
                            translationViewModel.setTargetLanguage(language)
                        },
                        modifier = Modifier.clickable(enabled = false) { /* Prevent clicks from propagating */ }
                    )
                }
            }
            
            // Error message display
            if (translationState.error != null) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { translationViewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text("Translation error: ${translationState.error}")
                }
            }
        }
    }
}