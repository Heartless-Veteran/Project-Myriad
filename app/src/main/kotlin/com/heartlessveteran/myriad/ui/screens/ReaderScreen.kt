package com.heartlessveteran.myriad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Basic Reader Screen - Phase 1 Implementation
 * Provides basic page rendering and navigation functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    mangaTitle: String = "Sample Manga",
    chapterTitle: String = "Chapter 1",
    currentPage: Int = 1,
    totalPages: Int = 20,
    onNavigateBack: () -> Unit = {},
    onPageChange: (Int) -> Unit = {}
) {
    var page by remember { mutableIntStateOf(currentPage) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = mangaTitle,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "$chapterTitle - Page $page of $totalPages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            ReaderControls(
                currentPage = page,
                totalPages = totalPages,
                onPageChange = { newPage ->
                    page = newPage
                    onPageChange(newPage)
                }
            )
        }
    ) { paddingValues ->
        // Progress indicator
        LinearProgressIndicator(
            progress = { page.toFloat() / totalPages },
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        )
        
        // Page content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ReaderPageContent(
                page = page,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ReaderControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    Surface(
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous page button
            IconButton(
                onClick = { 
                    if (currentPage > 1) onPageChange(currentPage - 1) 
                },
                enabled = currentPage > 1
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous Page"
                )
            }
            
            // Page info and slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currentPage.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
                
                Slider(
                    value = currentPage.toFloat(),
                    onValueChange = { onPageChange(it.toInt()) },
                    valueRange = 1f..totalPages.toFloat(),
                    steps = totalPages - 2,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = totalPages.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            // Next page button
            IconButton(
                onClick = { 
                    if (currentPage < totalPages) onPageChange(currentPage + 1) 
                },
                enabled = currentPage < totalPages
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next Page"
                )
            }
        }
    }
}

@Composable
private fun ReaderPageContent(
    page: Int,
    modifier: Modifier = Modifier
) {
    // Placeholder content for Phase 1
    // In complete implementation, this would display actual manga pages
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f), // Typical manga page ratio
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📖",
                    style = MaterialTheme.typography.displayLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Page $page",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Basic Reader Implementation",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Phase 1: Basic page rendering and navigation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Future: .cbz/.cbr file support, zoom controls, reading modes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}