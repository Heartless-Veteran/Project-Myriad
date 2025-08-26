package com.heartlessveteran.myriad.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Navigation event for handling navigation actions
 */
sealed interface NavigationEvent {
    data class NavigateTo(
        val destination: Destination,
        val navOptions: NavOptions? = null
    ) : NavigationEvent
    
    data object NavigateBack : NavigationEvent
    
    data class NavigateBackTo(
        val destination: Destination,
        val inclusive: Boolean = false
    ) : NavigationEvent
    
    data class NavigateAndClearBackStack(
        val destination: Destination
    ) : NavigationEvent
}

/**
 * Navigation state for tracking current location and history
 */
data class NavigationState(
    val currentDestination: Destination = Destination.Home,
    val previousDestination: Destination? = null,
    val canNavigateBack: Boolean = false,
    val navigationHistory: List<Destination> = emptyList()
)

/**
 * Service for handling programmatic navigation with type safety
 */
@Singleton
class NavigationService @Inject constructor() {
    
    private var navController: NavController? = null
    
    private val _navigationState = MutableStateFlow(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    
    private val _navigationEvents = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvents: StateFlow<NavigationEvent?> = _navigationEvents.asStateFlow()
    
    /**
     * Initialize the navigation service with NavController
     */
    fun initialize(navController: NavController) {
        this.navController = navController
        
        // Listen to destination changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateNavigationState(destination.route)
        }
    }
    
    /**
     * Navigate to a specific destination with type safety
     */
    fun navigateTo(
        destination: Destination, 
        navOptions: NavOptions? = null
    ) {
        _navigationEvents.value = NavigationEvent.NavigateTo(destination, navOptions)
        executeNavigation(destination, navOptions)
    }
    
    /**
     * Navigate back to previous screen
     */
    fun navigateBack() {
        _navigationEvents.value = NavigationEvent.NavigateBack
        navController?.navigateUp()
    }
    
    /**
     * Navigate back to specific destination
     */
    fun navigateBackTo(destination: Destination, inclusive: Boolean = false) {
        _navigationEvents.value = NavigationEvent.NavigateBackTo(destination, inclusive)
        
        val route = getRouteForDestination(destination)
        navController?.popBackStack(route, inclusive)
    }
    
    /**
     * Navigate and clear entire back stack
     */
    fun navigateAndClearBackStack(destination: Destination) {
        _navigationEvents.value = NavigationEvent.NavigateAndClearBackStack(destination)
        
        val navOptions = NavOptions.Builder()
            .setPopUpTo(navController?.graph?.startDestinationId ?: 0, true)
            .build()
        
        executeNavigation(destination, navOptions)
    }
    
    /**
     * Check if we can navigate back
     */
    fun canNavigateBack(): Boolean = navController?.previousBackStackEntry != null
    
    /**
     * Get current destination
     */
    fun getCurrentDestination(): Destination? {
        val currentRoute = navController?.currentDestination?.route
        return parseRouteToDestination(currentRoute)
    }
    
    /**
     * Navigate to manga reading screen
     */
    fun navigateToReading(mangaId: String, chapterId: String? = null, page: Int = 0) {
        navigateTo(Destination.Reading(mangaId, chapterId, page))
    }
    
    /**
     * Navigate to anime watching screen
     */
    fun navigateToWatching(animeId: String, episodeId: String? = null, timestamp: Long = 0) {
        navigateTo(Destination.Watching(animeId, episodeId, timestamp))
    }
    
    /**
     * Navigate to manga detail screen
     */
    fun navigateToMangaDetail(mangaId: String, sourceId: String? = null) {
        navigateTo(Destination.MangaDetail(mangaId, sourceId))
    }
    
    /**
     * Navigate to anime detail screen
     */
    fun navigateToAnimeDetail(animeId: String, sourceId: String? = null) {
        navigateTo(Destination.AnimeDetail(animeId, sourceId))
    }
    
    /**
     * Navigate to search screen
     */
    fun navigateToSearch(query: String = "", type: ContentType = ContentType.ALL, source: String? = null) {
        navigateTo(Destination.Search(query, type, source))
    }
    
    /**
     * Navigate to settings screen
     */
    fun navigateToSettings(section: SettingsSection = SettingsSection.GENERAL) {
        navigateTo(Destination.Settings(section))
    }
    
    /**
     * Clear navigation event after consumption
     */
    fun clearNavigationEvent() {
        _navigationEvents.value = null
    }
    
    /**
     * Execute the actual navigation
     */
    private fun executeNavigation(destination: Destination, navOptions: NavOptions?) {
        val route = getRouteForDestination(destination)
        
        try {
            if (validateNavigation(destination)) {
                navController?.navigate(route, navOptions)
            } else {
                // Log validation error or handle invalid navigation
                android.util.Log.w("NavigationService", "Invalid navigation to $destination")
            }
        } catch (e: Exception) {
            // Handle navigation errors
            android.util.Log.e("NavigationService", "Navigation error", e)
        }
    }
    
    /**
     * Get navigation route string for destination
     */
    private fun getRouteForDestination(destination: Destination): String {
        return when (destination) {
            is Destination.Home -> NavigationRoutes.HOME
            is Destination.MangaLibrary -> NavigationRoutes.MANGA_LIBRARY
            is Destination.AnimeLibrary -> NavigationRoutes.ANIME_LIBRARY
            is Destination.Browse -> NavigationRoutes.BROWSE
            is Destination.AICore -> NavigationRoutes.AI_CORE
            is Destination.Reading -> Destination.Reading.createRoute(destination.mangaId, destination.chapterId, destination.page)
            is Destination.Watching -> Destination.Watching.createRoute(destination.animeId, destination.episodeId, destination.timestamp)
            is Destination.MangaDetail -> Destination.MangaDetail.createRoute(destination.mangaId, destination.sourceId)
            is Destination.AnimeDetail -> Destination.AnimeDetail.createRoute(destination.animeId, destination.sourceId)
            is Destination.Search -> Destination.Search.createRoute(destination.query, destination.type, destination.source)
            is Destination.Settings -> Destination.Settings.createRoute(destination.section)
        }
    }
    
    /**
     * Parse route string back to destination
     */
    private fun parseRouteToDestination(route: String?): Destination? {
        return when {
            route == null -> null
            route == NavigationRoutes.HOME -> Destination.Home
            route == NavigationRoutes.MANGA_LIBRARY -> Destination.MangaLibrary
            route == NavigationRoutes.ANIME_LIBRARY -> Destination.AnimeLibrary
            route == NavigationRoutes.BROWSE -> Destination.Browse
            route == NavigationRoutes.AI_CORE -> Destination.AICore
            route.startsWith("reading/") -> parseReadingRoute(route)
            route.startsWith("watching/") -> parseWatchingRoute(route)
            route.startsWith("manga_detail/") -> parseMangaDetailRoute(route)
            route.startsWith("anime_detail/") -> parseAnimeDetailRoute(route)
            route.startsWith("search") -> parseSearchRoute(route)
            route.startsWith("settings/") -> parseSettingsRoute(route)
            else -> null
        }
    }
    
    /**
     * Validate navigation parameters
     */
    private fun validateNavigation(destination: Destination): Boolean {
        return when (destination) {
            is Destination.Reading -> NavigationValidator.validateMangaId(destination.mangaId) &&
                    NavigationValidator.validatePage(destination.page.toString())
            is Destination.Watching -> NavigationValidator.validateAnimeId(destination.animeId) &&
                    NavigationValidator.validateTimestamp(destination.timestamp.toString())
            is Destination.MangaDetail -> NavigationValidator.validateMangaId(destination.mangaId)
            is Destination.AnimeDetail -> NavigationValidator.validateAnimeId(destination.animeId)
            is Destination.Search -> NavigationValidator.validateSearchQuery(destination.query) &&
                    NavigationValidator.validateContentType(destination.type.name)
            else -> true
        }
    }
    
    /**
     * Update navigation state
     */
    private fun updateNavigationState(route: String?) {
        val currentDestination = parseRouteToDestination(route)
        if (currentDestination != null) {
            val previousDestination = _navigationState.value.currentDestination
            val canNavigateBack = canNavigateBack()
            val navigationHistory = _navigationState.value.navigationHistory.toMutableList().apply {
                add(currentDestination)
                if (size > 10) removeAt(0) // Keep only last 10 destinations
            }
            
            _navigationState.value = NavigationState(
                currentDestination = currentDestination,
                previousDestination = if (currentDestination != previousDestination) previousDestination else null,
                canNavigateBack = canNavigateBack,
                navigationHistory = navigationHistory
            )
        }
    }
    
    // Helper methods for parsing specific route types
    private fun parseReadingRoute(route: String): Destination.Reading? {
        // Implementation would parse the route parameters
        // This is a simplified version
        return null // TODO: Implement route parsing
    }
    
    private fun parseWatchingRoute(route: String): Destination.Watching? {
        return null // TODO: Implement route parsing
    }
    
    private fun parseMangaDetailRoute(route: String): Destination.MangaDetail? {
        return null // TODO: Implement route parsing
    }
    
    private fun parseAnimeDetailRoute(route: String): Destination.AnimeDetail? {
        return null // TODO: Implement route parsing
    }
    
    private fun parseSearchRoute(route: String): Destination.Search? {
        return null // TODO: Implement route parsing
    }
    
    private fun parseSettingsRoute(route: String): Destination.Settings? {
        return null // TODO: Implement route parsing
    }
}