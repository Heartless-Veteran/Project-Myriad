package com.heartlessveteran.myriad.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.heartlessveteran.myriad.ui.screens.*

/**
 * Bottom navigation item data class
 */
data class BottomNavItem(
    val destination: Destination,
    val icon: ImageVector,
    val label: String
)

/**
 * Bottom navigation items configuration
 */
val bottomNavItems = listOf(
    BottomNavItem(Destination.Home, Icons.Default.Home, "Home"),
    BottomNavItem(Destination.MangaLibrary, Icons.Default.LibraryBooks, "Manga"),
    BottomNavItem(Destination.AnimeLibrary, Icons.Default.PlayArrow, "Anime"),
    BottomNavItem(Destination.Browse, Icons.Default.Search, "Browse"),
    BottomNavItem(Destination.AICore, Icons.Default.Psychology, "AI Core")
)

/**
 * Enhanced navigation component with type-safe navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMyriadNavigation(
    navController: NavHostController,
    navigationService: NavigationService
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Initialize navigation service
    LaunchedEffect(navController) {
        navigationService.initialize(navController)
    }
    
    // Observe navigation events
    val navigationEvent by navigationService.navigationEvents.collectAsState()
    
    Scaffold(
        bottomBar = {
            // Show bottom navigation only for main screens
            val shouldShowBottomNav = bottomNavItems.any { item ->
                currentDestination?.hierarchy?.any { 
                    it.route == getRoutePattern(item.destination)
                } == true
            }
            
            if (shouldShowBottomNav) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { 
                            it.route == getRoutePattern(item.destination)
                        } == true
                        
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    imageVector = item.icon, 
                                    contentDescription = item.label
                                ) 
                            },
                            label = { Text(item.label) },
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) {
                                    navigationService.navigateTo(
                                        destination = item.destination,
                                        navOptions = androidx.navigation.NavOptions.Builder()
                                            .setPopUpTo(
                                                navController.graph.findStartDestination().id,
                                                saveState = true
                                            )
                                            .setLaunchSingleTop(true)
                                            .setRestoreState(true)
                                            .build()
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationRoutes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Main screens
            composable(NavigationRoutes.HOME) {
                HomeScreen(
                    onNavigateToManga = { navigationService.navigateTo(Destination.MangaLibrary) },
                    onNavigateToAnime = { navigationService.navigateTo(Destination.AnimeLibrary) },
                    onNavigateToAI = { navigationService.navigateTo(Destination.AICore) }
                )
            }
            
            composable(NavigationRoutes.MANGA_LIBRARY) {
                MangaLibraryScreen(
                    onMangaClick = { mangaId ->
                        navigationService.navigateToMangaDetail(mangaId)
                    },
                    onReadManga = { mangaId, chapterId ->
                        navigationService.navigateToReading(mangaId, chapterId)
                    }
                )
            }
            
            composable(NavigationRoutes.ANIME_LIBRARY) {
                AnimeLibraryScreen(
                    onAnimeClick = { animeId ->
                        navigationService.navigateToAnimeDetail(animeId)
                    },
                    onWatchAnime = { animeId, episodeId ->
                        navigationService.navigateToWatching(animeId, episodeId)
                    }
                )
            }
            
            composable(NavigationRoutes.BROWSE) {
                BrowseScreen(
                    onSearch = { query, type ->
                        navigationService.navigateToSearch(query, type)
                    },
                    onMangaClick = { mangaId, sourceId ->
                        navigationService.navigateToMangaDetail(mangaId, sourceId)
                    },
                    onAnimeClick = { animeId, sourceId ->
                        navigationService.navigateToAnimeDetail(animeId, sourceId)
                    }
                )
            }
            
            composable(NavigationRoutes.AI_CORE) {
                AICoreScreen(
                    onTranslateImage = { /* Handle image translation */ },
                    onAnalyzeArt = { /* Handle art analysis */ },
                    onSettings = {
                        navigationService.navigateToSettings(SettingsSection.AI)
                    }
                )
            }
            
            // Detail screens with parameter validation
            composable(
                route = NavigationRoutes.MANGA_DETAIL,
                arguments = listOf(
                    navArgument(NavigationParams.MANGA_ID) { 
                        type = NavType.StringType
                        nullable = false
                    },
                    navArgument(NavigationParams.SOURCE_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val mangaId = backStackEntry.arguments?.getString(NavigationParams.MANGA_ID)
                val sourceId = backStackEntry.arguments?.getString(NavigationParams.SOURCE_ID)
                
                if (NavigationValidator.validateMangaId(mangaId)) {
                    MangaDetailScreen(
                        mangaId = mangaId!!,
                        sourceId = sourceId,
                        onReadClick = { chapterId ->
                            navigationService.navigateToReading(mangaId, chapterId)
                        },
                        onBackClick = {
                            navigationService.navigateBack()
                        }
                    )
                } else {
                    // Show error screen or navigate back
                    LaunchedEffect(Unit) {
                        navigationService.navigateBack()
                    }
                }
            }
            
            composable(
                route = NavigationRoutes.ANIME_DETAIL,
                arguments = listOf(
                    navArgument(NavigationParams.ANIME_ID) { 
                        type = NavType.StringType
                        nullable = false
                    },
                    navArgument(NavigationParams.SOURCE_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getString(NavigationParams.ANIME_ID)
                val sourceId = backStackEntry.arguments?.getString(NavigationParams.SOURCE_ID)
                
                if (NavigationValidator.validateAnimeId(animeId)) {
                    AnimeDetailScreen(
                        animeId = animeId!!,
                        sourceId = sourceId,
                        onWatchClick = { episodeId ->
                            navigationService.navigateToWatching(animeId, episodeId)
                        },
                        onBackClick = {
                            navigationService.navigateBack()
                        }
                    )
                } else {
                    // Show error screen or navigate back
                    LaunchedEffect(Unit) {
                        navigationService.navigateBack()
                    }
                }
            }
            
            // Reading/Watching screens
            composable(
                route = NavigationRoutes.READING,
                arguments = listOf(
                    navArgument(NavigationParams.MANGA_ID) { 
                        type = NavType.StringType
                        nullable = false
                    },
                    navArgument(NavigationParams.CHAPTER_ID) { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument(NavigationParams.PAGE) {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val mangaId = backStackEntry.arguments?.getString(NavigationParams.MANGA_ID)
                val chapterId = backStackEntry.arguments?.getString(NavigationParams.CHAPTER_ID)
                val page = backStackEntry.arguments?.getInt(NavigationParams.PAGE, 0)
                
                if (NavigationValidator.validateMangaId(mangaId) && 
                    NavigationValidator.validatePage(page.toString())) {
                    ReadingScreen(
                        mangaId = mangaId!!,
                        chapterId = chapterId,
                        initialPage = page ?: 0,
                        onBackClick = {
                            navigationService.navigateBack()
                        },
                        onChapterChange = { newChapterId ->
                            // Navigate to new chapter while maintaining current position
                            navigationService.navigateTo(
                                Destination.Reading(mangaId, newChapterId, 0)
                            )
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navigationService.navigateBack()
                    }
                }
            }
            
            composable(
                route = NavigationRoutes.WATCHING,
                arguments = listOf(
                    navArgument(NavigationParams.ANIME_ID) { 
                        type = NavType.StringType
                        nullable = false
                    },
                    navArgument(NavigationParams.EPISODE_ID) { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument(NavigationParams.TIMESTAMP) {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getString(NavigationParams.ANIME_ID)
                val episodeId = backStackEntry.arguments?.getString(NavigationParams.EPISODE_ID)
                val timestamp = backStackEntry.arguments?.getLong(NavigationParams.TIMESTAMP, 0L)
                
                if (NavigationValidator.validateAnimeId(animeId) && 
                    NavigationValidator.validateTimestamp(timestamp.toString())) {
                    WatchingScreen(
                        animeId = animeId!!,
                        episodeId = episodeId,
                        initialTimestamp = timestamp ?: 0L,
                        onBackClick = {
                            navigationService.navigateBack()
                        },
                        onEpisodeChange = { newEpisodeId ->
                            navigationService.navigateTo(
                                Destination.Watching(animeId, newEpisodeId, 0L)
                            )
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navigationService.navigateBack()
                    }
                }
            }
            
            // Search screen
            composable(
                route = NavigationRoutes.SEARCH,
                arguments = listOf(
                    navArgument(NavigationParams.QUERY) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(NavigationParams.TYPE) {
                        type = NavType.StringType
                        defaultValue = ContentType.ALL.name
                    },
                    navArgument(NavigationParams.SOURCE) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val query = backStackEntry.arguments?.getString(NavigationParams.QUERY) ?: ""
                val typeString = backStackEntry.arguments?.getString(NavigationParams.TYPE) ?: ContentType.ALL.name
                val source = backStackEntry.arguments?.getString(NavigationParams.SOURCE)
                
                val contentType = try {
                    ContentType.valueOf(typeString.uppercase())
                } catch (e: IllegalArgumentException) {
                    ContentType.ALL
                }
                
                SearchScreen(
                    initialQuery = query,
                    initialType = contentType,
                    initialSource = source,
                    onMangaClick = { mangaId, sourceId ->
                        navigationService.navigateToMangaDetail(mangaId, sourceId)
                    },
                    onAnimeClick = { animeId, sourceId ->
                        navigationService.navigateToAnimeDetail(animeId, sourceId)
                    },
                    onBackClick = {
                        navigationService.navigateBack()
                    }
                )
            }
            
            // Settings screen
            composable(
                route = NavigationRoutes.SETTINGS,
                arguments = listOf(
                    navArgument(NavigationParams.SECTION) {
                        type = NavType.StringType
                        defaultValue = SettingsSection.GENERAL.name.lowercase()
                    }
                )
            ) { backStackEntry ->
                val sectionString = backStackEntry.arguments?.getString(NavigationParams.SECTION) 
                    ?: SettingsSection.GENERAL.name.lowercase()
                
                val section = try {
                    SettingsSection.valueOf(sectionString.uppercase())
                } catch (e: IllegalArgumentException) {
                    SettingsSection.GENERAL
                }
                
                SettingsScreen(
                    initialSection = section,
                    onBackClick = {
                        navigationService.navigateBack()
                    },
                    onSectionChange = { newSection ->
                        navigationService.navigateTo(Destination.Settings(newSection))
                    }
                )
            }
        }
    }
}

/**
 * Get route pattern for destination
 */
private fun getRoutePattern(destination: Destination): String {
    return when (destination) {
        is Destination.Home -> NavigationRoutes.HOME
        is Destination.MangaLibrary -> NavigationRoutes.MANGA_LIBRARY
        is Destination.AnimeLibrary -> NavigationRoutes.ANIME_LIBRARY
        is Destination.Browse -> NavigationRoutes.BROWSE
        is Destination.AICore -> NavigationRoutes.AI_CORE
        else -> ""
    }
}

// Import statements for navigation arguments
import androidx.navigation.NavType
import androidx.navigation.navArgument