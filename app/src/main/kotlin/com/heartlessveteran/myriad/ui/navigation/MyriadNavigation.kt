package com.heartlessveteran.myriad.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.heartlessveteran.myriad.ui.screens.*
import com.heartlessveteran.myriad.navigation.SettingsSection

/**
 * Main navigation routes for the app
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object MangaLibrary : Screen("manga_library", "Manga", Icons.Default.LibraryBooks)
    object AnimeLibrary : Screen("anime_library", "Anime", Icons.Default.PlayArrow)
    object Browse : Screen("browse", "Browse", Icons.Default.Search)
    object AICore : Screen("ai_core", "AI Core", Icons.Default.Psychology)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Reading : Screen("reading/{mangaId}", "Reading", Icons.Default.AutoStories) {
        fun createRoute(mangaId: String) = "reading/$mangaId"
    }
    object Watching : Screen("watching/{animeId}", "Watching", Icons.Default.PlayArrow) {
        fun createRoute(animeId: String) = "watching/$animeId"
    }
}

/**
 * Bottom navigation items
 */
val bottomNavItems = listOf(
    Screen.Home,
    Screen.MangaLibrary,
    Screen.AnimeLibrary,
    Screen.Browse,
    Screen.AICore,
    Screen.Settings
)

/**
 * Main navigation component with bottom navigation bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyriadNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    Scaffold(
        bottomBar = {
            // Only show bottom navigation for main screens
            if (currentRoute in bottomNavItems.map { it.route }) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
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
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToManga = { navController.navigate(Screen.MangaLibrary.route) },
                    onNavigateToAnime = { navController.navigate(Screen.AnimeLibrary.route) },
                    onNavigateToAI = { navController.navigate(Screen.AICore.route) }
                )
            }
            
            composable(Screen.MangaLibrary.route) {
                MangaLibraryScreen(
                    onMangaClick = { mangaId ->
                        navController.navigate(Screen.Reading.createRoute(mangaId))
                    }
                )
            }
            
            composable(Screen.AnimeLibrary.route) {
                AnimeLibraryScreen(
                    onAnimeClick = { animeId ->
                        navController.navigate(Screen.Watching.createRoute(animeId))
                    }
                )
            }
            
            composable(Screen.Browse.route) {
                BrowseScreen()
            }
            
            composable(Screen.AICore.route) {
                AICoreScreen()
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(
                    initialSection = SettingsSection.GENERAL, // or appropriate default section
                    onBackClick = { navController.popBackStack() },
                    onSectionChange = { /* handle section change if needed */ }
                )
            }
            
            composable(Screen.Reading.route) { backStackEntry ->
                val mangaId = backStackEntry.arguments?.getString("mangaId") ?: return@composable
                ReadingScreen(
                    mangaId = mangaId,
                    onBackPress = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Watching.route) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getString("animeId") ?: return@composable
                WatchingScreen(
                    animeId = animeId,
                    onBackPress = { navController.popBackStack() }
                )
            }
        }
    }
}