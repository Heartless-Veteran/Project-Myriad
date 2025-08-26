package com.heartlessveteran.myriad.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
 * Browse Screen - For discovering new content and file management
 * Phase 2: Enhanced with local file import capabilities
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Online", "Local Files", "Import")
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { 
                Text(
                    text = "Browse & Discover",
                    fontWeight = FontWeight.Bold
                )
            }
        )
        
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        when (selectedTab) {
            0 -> OnlineBrowseContent()
            1 -> LocalFilesContent()
            2 -> ImportContent()
        }
    }
}

@Composable
private fun OnlineBrowseContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Online Browse",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Online Content Discovery",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Online content sources will be available in the next development phase",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun LocalFilesContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Local Library",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Library Stats",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Library Statistics",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 0 Manga files (.cbz/.cbr)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• 0 Anime files (.mp4/.mkv/.avi)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Total storage: 0 MB",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "File Management",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "File management system is currently in development. Basic file import and scanning capabilities are being implemented in this phase.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Import Media Files",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        item {
            Card(
                onClick = { /* TODO: Implement manga import */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LibraryBooks,
                        contentDescription = "Import Manga",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Import Manga",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Select .cbz or .cbr files",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        item {
            Card(
                onClick = { /* TODO: Implement anime import */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Import Anime",
                        tint = AnimeAccent,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Import Anime",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Select .mp4, .mkv, or .avi files",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        item {
            Card(
                onClick = { /* TODO: Implement folder scanning */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Scan Folder",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Scan Folder",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Automatically scan a folder for media files",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Supported Formats",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Manga: .cbz (ZIP), .cbr (RAR)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Anime: .mp4, .mkv, .avi",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Note: File import functionality is currently being developed and will be fully functional in future updates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * AI Core Screen - For AI-powered features
 * Phase 2: Enhanced with detailed AI feature information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICoreScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = com.heartlessveteran.myriad.ui.theme.AIAccent.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Core",
                        modifier = Modifier.size(48.dp),
                        tint = com.heartlessveteran.myriad.ui.theme.AIAccent
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "AI-Powered Features",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = com.heartlessveteran.myriad.ui.theme.AIAccent
                    )
                    Text(
                        text = "Intelligent tools to enhance your manga and anime experience",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        item {
            Text(
                text = "Available Features",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // OCR Translation Feature
        item {
            AIFeatureCard(
                icon = Icons.Default.Translate,
                title = "OCR Translation",
                description = "Real-time translation of manga text using advanced OCR technology",
                status = "Coming in Phase 3",
                isEnabled = false
            )
        }
        
        // Art Style Matching Feature
        item {
            AIFeatureCard(
                icon = Icons.Default.Palette,
                title = "Art Style Matching",
                description = "Intelligent categorization based on art style analysis",
                status = "Coming in Phase 3",
                isEnabled = false
            )
        }
        
        // Smart Recommendations Feature
        item {
            AIFeatureCard(
                icon = Icons.Default.Recommend,
                title = "Smart Recommendations",
                description = "AI-driven content suggestions based on your reading patterns",
                status = "Coming in Phase 3",
                isEnabled = false
            )
        }
        
        // Scene Analysis Feature
        item {
            AIFeatureCard(
                icon = Icons.Default.Analytics,
                title = "Scene Analysis",
                description = "Automatic chapter and scene recognition for better navigation",
                status = "Coming in Phase 3",
                isEnabled = false
            )
        }
        
        item {
            Text(
                text = "Experimental Features",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // Mood Tracker Feature
        item {
            AIFeatureCard(
                icon = Icons.Default.SentimentSatisfied,
                title = "Mood Tracker",
                description = "Track your emotional responses to different series",
                status = "Experimental",
                isEnabled = false
            )
        }
        
        // Voice Reader Feature
        item {
            AIFeatureCard(
                icon = Icons.Default.RecordVoiceOver,
                title = "AI Voice Reader",
                description = "Text-to-speech narration for manga content",
                status = "Future Update",
                isEnabled = false
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Development Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "AI features are scheduled for Phase 3 (Q3 2024) of the development roadmap. These features will leverage cutting-edge machine learning models for OCR, image analysis, and recommendation systems.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Current focus is on Phase 2: Core Features - building essential manga/anime management and reader capabilities.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AIFeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    status: String,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (isEnabled) { { /* TODO: Navigate to feature */ } } else { {} },
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = if (isEnabled) 
                    com.heartlessveteran.myriad.ui.theme.AIAccent 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isEnabled) 
                        MaterialTheme.colorScheme.onSurface 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isEnabled) 
                        com.heartlessveteran.myriad.ui.theme.AIAccent 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Reading Screen - For reading manga
 * Phase 2: Enhanced reader with basic functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(
    mangaId: String,
    onBackPress: () -> Unit
) {
    var currentPage by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(10) } // Mock data
    var isMenuVisible by remember { mutableStateOf(false) }
    
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
                        IconButton(onClick = { /* TODO: Settings */ }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (isMenuVisible) {
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { 
                                if (currentPage > 1) currentPage-- 
                            },
                            enabled = currentPage > 1
                        ) {
                            Icon(Icons.Default.NavigateBefore, contentDescription = "Previous Page")
                        }
                        
                        Text(
                            text = "$currentPage / $totalPages",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        IconButton(
                            onClick = { 
                                if (currentPage < totalPages) currentPage++ 
                            },
                            enabled = currentPage < totalPages
                        ) {
                            Icon(Icons.Default.NavigateNext, contentDescription = "Next Page")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clickable { isMenuVisible = !isMenuVisible },
            contentAlignment = Alignment.Center
        ) {
            // Mock manga page display
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(0.7f),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Manga: $mangaId",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Page $currentPage",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "[Mock Page Content]",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap screen to toggle controls",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Swipe or use buttons to navigate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
 * Phase 2: Enhanced settings with multiple sections
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    initialSection: SettingsSection = SettingsSection.GENERAL,
    onBackClick: () -> Unit = {},
    onSectionChange: (SettingsSection) -> Unit = {}
) {
    var currentSection by remember { mutableStateOf(initialSection) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { 
                Text(
                    text = "Settings - ${currentSection.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    fontWeight = FontWeight.Bold
                )
            }
        )
        
        Row(modifier = Modifier.fillMaxSize()) {
            // Section navigation sidebar
            NavigationRail(
                modifier = Modifier.width(120.dp)
            ) {
                com.heartlessveteran.myriad.navigation.SettingsSection.values().forEach { section ->
                    NavigationRailItem(
                        icon = { 
                            Icon(
                                imageVector = getSettingSectionIcon(section),
                                contentDescription = section.name
                            )
                        },
                        label = { 
                            Text(
                                text = section.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = currentSection == section,
                        onClick = {
                            currentSection = section
                            onSectionChange(section)
                        }
                    )
                }
            }
            
            // Settings content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (currentSection) {
                    com.heartlessveteran.myriad.navigation.SettingsSection.GENERAL -> GeneralSettings()
                    com.heartlessveteran.myriad.navigation.SettingsSection.READING -> ReadingSettings()
                    com.heartlessveteran.myriad.navigation.SettingsSection.WATCHING -> WatchingSettings()
                    com.heartlessveteran.myriad.navigation.SettingsSection.SOURCES -> SourcesSettings()
                    com.heartlessveteran.myriad.navigation.SettingsSection.STORAGE -> StorageSettings()
                    com.heartlessveteran.myriad.navigation.SettingsSection.AI -> AISettings()
                    com.heartlessveteran.myriad.navigation.SettingsSection.ABOUT -> AboutSettings()
                }
            }
        }
    }
}

@Composable
private fun getSettingSectionIcon(section: com.heartlessveteran.myriad.navigation.SettingsSection): androidx.compose.ui.graphics.vector.ImageVector {
    return when (section) {
        com.heartlessveteran.myriad.navigation.SettingsSection.GENERAL -> Icons.Default.Settings
        com.heartlessveteran.myriad.navigation.SettingsSection.READING -> Icons.Default.AutoStories
        com.heartlessveteran.myriad.navigation.SettingsSection.WATCHING -> Icons.Default.PlayArrow
        com.heartlessveteran.myriad.navigation.SettingsSection.SOURCES -> Icons.Default.Search
        com.heartlessveteran.myriad.navigation.SettingsSection.STORAGE -> Icons.Default.Folder
        com.heartlessveteran.myriad.navigation.SettingsSection.AI -> Icons.Default.Psychology
        com.heartlessveteran.myriad.navigation.SettingsSection.ABOUT -> Icons.Default.Info
    }
}

@Composable
private fun GeneralSettings() {
    SettingsSection(
        title = "General Settings",
        description = "App-wide preferences and behavior"
    ) {
        SettingsItem(
            title = "Theme",
            description = "Choose your preferred theme",
            onClick = { /* TODO: Implement theme selection */ }
        )
        SettingsItem(
            title = "Language",
            description = "Select app language",
            onClick = { /* TODO: Implement language selection */ }
        )
        SettingsItem(
            title = "Notifications",
            description = "Manage notification preferences",
            onClick = { /* TODO: Implement notification settings */ }
        )
    }
}

@Composable
private fun ReadingSettings() {
    SettingsSection(
        title = "Reading Settings",
        description = "Customize your manga reading experience"
    ) {
        SettingsItem(
            title = "Reading Mode",
            description = "Default reading mode (Single, Double, Continuous)",
            onClick = { /* TODO: Implement reading mode selection */ }
        )
        SettingsItem(
            title = "Page Direction",
            description = "Left-to-right or right-to-left",
            onClick = { /* TODO: Implement page direction */ }
        )
        SettingsItem(
            title = "Zoom Settings",
            description = "Default zoom behavior",
            onClick = { /* TODO: Implement zoom settings */ }
        )
    }
}

@Composable
private fun WatchingSettings() {
    SettingsSection(
        title = "Watching Settings", 
        description = "Customize your anime viewing experience"
    ) {
        SettingsItem(
            title = "Video Quality",
            description = "Preferred video quality",
            onClick = { /* TODO: Implement video quality */ }
        )
        SettingsItem(
            title = "Subtitle Settings",
            description = "Subtitle preferences",
            onClick = { /* TODO: Implement subtitle settings */ }
        )
        SettingsItem(
            title = "Auto-play",
            description = "Automatically play next episode",
            onClick = { /* TODO: Implement auto-play */ }
        )
    }
}

@Composable
private fun SourcesSettings() {
    SettingsSection(
        title = "Content Sources",
        description = "Manage your content providers"
    ) {
        Text(
            text = "Online content sources will be available in the next development phase",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun StorageSettings() {
    SettingsSection(
        title = "Storage Settings",
        description = "Manage local storage and cache"
    ) {
        SettingsItem(
            title = "Library Location",
            description = "Choose where your media is stored",
            onClick = { /* TODO: Implement library location */ }
        )
        SettingsItem(
            title = "Cache Size",
            description = "Manage app cache",
            onClick = { /* TODO: Implement cache management */ }
        )
        SettingsItem(
            title = "Auto-cleanup",
            description = "Automatically clean up old files",
            onClick = { /* TODO: Implement auto-cleanup */ }
        )
    }
}

@Composable
private fun AISettings() {
    SettingsSection(
        title = "AI Features",
        description = "Configure AI-powered capabilities"
    ) {
        SettingsItem(
            title = "OCR Translation",
            description = "Automatic text translation settings",
            onClick = { /* TODO: Implement OCR settings */ }
        )
        SettingsItem(
            title = "Recommendations",
            description = "AI-powered content suggestions",
            onClick = { /* TODO: Implement recommendation settings */ }
        )
        Text(
            text = "AI features are currently in development",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun AboutSettings() {
    SettingsSection(
        title = "About Project Myriad",
        description = "App information and credits"
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Project Myriad",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "The Definitive Manga and Anime Platform",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Version 1.0.0 - Phase 2",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Built with modern Android architecture using Jetpack Compose, Room database, and Material 3 design.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun SettingsItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}