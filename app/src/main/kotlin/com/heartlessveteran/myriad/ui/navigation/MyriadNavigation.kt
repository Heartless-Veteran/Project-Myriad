package com.heartlessveteran.myriad.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heartlessveteran.myriad.ui.screens.AnimeLibraryScreen
import com.heartlessveteran.myriad.ui.screens.LibraryScreen

/**
 * Main navigation component for Project Myriad
 * Provides access to all major features: Library, Anime, Reader
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyriadNavigation() {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Project Myriad",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Menu, contentDescription = "Manga") },
                    label = { Text("Manga") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Anime") },
                    label = { Text("Anime") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> HomeScreen(
                modifier = Modifier.padding(paddingValues)
            )
            1 -> LibraryScreen(
                modifier = Modifier.padding(paddingValues)
            )
            2 -> AnimeLibraryScreen(
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "🎌 Project Myriad",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "The Definitive Manga and Anime Platform",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📋 Phase 1 - Foundation Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                StatusItem("✅ Core Architecture", "Clean Architecture + MVVM")
                StatusItem("✅ UI Framework", "Jetpack Compose with Material 3")
                StatusItem("✅ Navigation", "Production ready MainActivity")
                StatusItem("🔄 Reader System", "Basic implementation")
                StatusItem("🔄 File Management", "Import/Export system")
                StatusItem("🔄 Error Handling", "Global error framework")
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🚀 Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text("• Navigate to Manga tab to browse manga library")
                Text("• Navigate to Anime tab to watch anime content")
                Text("• File import/export functionality in development")
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "Phase 1 Foundation Implementation",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusItem(status: String, description: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}