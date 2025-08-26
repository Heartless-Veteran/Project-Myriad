package com.heartlessveteran.myriad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heartlessveteran.myriad.ui.theme.AnimeAccent

/**
 * Anime Library Screen - Similar to manga library but for anime content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeLibraryScreen(
    onAnimeClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { 
                Text(
                    text = "Anime Library",
                    color = AnimeAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        )
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Anime Library",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Coming soon in the next phase",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Browse Screen - For discovering new content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { 
                Text(
                    text = "Browse",
                    fontWeight = FontWeight.Bold
                )
            }
        )
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Browse & Discover",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Content discovery features coming soon",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * AI Core Screen - For AI-powered features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICoreScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { 
                Text(
                    text = "AI Core",
                    color = com.heartlessveteran.myriad.ui.theme.AIAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        )
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "AI-Powered Features",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "OCR Translation, Art Style Matching, and more coming soon",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Reading Screen - For reading manga
 */
@Composable
fun ReadingScreen(
    mangaId: String,
    onBackPress: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Reading Screen",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Manga ID: $mangaId",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onBackPress) {
                Text("Back to Library")
            }
        }
    }
}

/**
 * Watching Screen - For watching anime
 */
@Composable
fun WatchingScreen(
    animeId: String,
    onBackPress: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Watching Screen",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Anime ID: $animeId",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onBackPress) {
                Text("Back to Library")
            }
        }
    }
}

/**
 * Settings Screen - Configuration and preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    initialSection: com.heartlessveteran.myriad.navigation.SettingsSection = com.heartlessveteran.myriad.navigation.SettingsSection.GENERAL,
    onBackClick: () -> Unit = {},
    onSectionChange: (com.heartlessveteran.myriad.navigation.SettingsSection) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { 
                Text(
                    text = "Settings - ${initialSection.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    fontWeight = FontWeight.Bold
                )
            }
        )
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Section: ${initialSection.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Enhanced settings coming in Phase 2",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Button(onClick = onBackClick) {
                    Text("Back")
                }
            }
        }
    }
}