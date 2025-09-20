package com.heartlessveteran.myriad.navigation

import android.net.Uri
import android.util.Log
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
        val navOptions: NavOptions? = null,
    ) : NavigationEvent

    data object NavigateBack : NavigationEvent

    data class NavigateBackTo(
        val destination: Destination,
        val inclusive: Boolean = false,
    ) : NavigationEvent

    data class NavigateAndClearBackStack(
        val destination: Destination,
    ) : NavigationEvent
}

/**
 * Navigation state for tracking current location and history
 */
data class NavigationState(
    val currentDestination: Destination = Destination.Home,
    val previousDestination: Destination? = null,
    val canNavigateBack: Boolean = false,
    val navigationHistory: List<Destination> = emptyList(),
)

/**
 * Service for handling programmatic navigation with type safety
 */
@Singleton
class NavigationService
    @Inject
    constructor() {
        private var navController: NavController? = null

        private val _navigationState = MutableStateFlow(NavigationState())
        val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

        private val _navigationEvents = MutableStateFlow<NavigationEvent?>(null)
        val navigationEvents: StateFlow<NavigationEvent?> = _navigationEvents.asStateFlow()

        /**
         * Initializes the navigation service with the provided NavController and begins observing destination changes.
         *
         * Sets the internal NavController reference and registers a listener that updates the service's NavigationState
         * whenever the NavController's current route changes.
         */
        fun initialize(navController: NavController) {
            this.navController = navController

            // Listen to destination changes
            navController.addOnDestinationChangedListener { _, destination, _ ->
                updateNavigationState(destination.route)
            }
        }

        /**
         * Requests navigation to the given Destination and immediately attempts to perform it.
         *
         * Emits a NavigationEvent.NavigateTo and executes navigation using the current NavController.
         *
         * @param destination The destination to navigate to.
         * @param navOptions Optional NavOptions to apply to the navigation call (e.g., launchSingleTop, popUpTo).
         */
        fun navigateTo(
            destination: Destination,
            navOptions: NavOptions? = null,
        ) {
            _navigationEvents.value = NavigationEvent.NavigateTo(destination, navOptions)
            executeNavigation(destination, navOptions)
        }

        /**
         * Requests navigation to go back one step.
         *
         * Emits a NavigateBack event on the navigationEvents StateFlow and attempts to call `navigateUp()` on the stored NavController.
         * If no NavController is initialized, only the event is emitted.
         */
        fun navigateBack() {
            _navigationEvents.value = NavigationEvent.NavigateBack
            navController?.navigateUp()
        }

        /**
         * Pops the navigation back stack to the specified destination and emits a corresponding navigation event.
         *
         * Emits a NavigateBackTo event with the provided destination and inclusive flag, then resolves the
         * destination to a route and calls NavController.popBackStack(route, inclusive).
         *
         * @param destination The target Destination to pop back to; resolved to its navigation route.
         * @param inclusive If true, the target destination itself will also be popped; otherwise it will remain.
         */
        fun navigateBackTo(
            destination: Destination,
            inclusive: Boolean = false,
        ) {
            _navigationEvents.value = NavigationEvent.NavigateBackTo(destination, inclusive)

            val route = getRouteForDestination(destination)
            navController?.popBackStack(route, inclusive)
        }

        /**
         * Navigates to the given destination and clears the entire back stack.
         *
         * The back stack is popped up to the navigation graph's start destination (inclusive)
         * before navigating to `destination`. Emits a NavigateAndClearBackStack event.
         *
         * @param destination The target Destination to navigate to after clearing the back stack.
         */
        fun navigateAndClearBackStack(destination: Destination) {
            _navigationEvents.value = NavigationEvent.NavigateAndClearBackStack(destination)

            val navOptions =
                NavOptions
                    .Builder()
                    .setPopUpTo(navController?.graph?.startDestinationId ?: 0, true)
                    .build()

            executeNavigation(destination, navOptions)
        }

        /**
         * Returns true if the navigation controller has a previous back stack entry.
         *
         * @return `true` when a previous destination exists and back navigation is possible; `false` otherwise.
         */
        fun canNavigateBack(): Boolean = navController?.previousBackStackEntry != null

        /**
         * Returns the current Destination represented by the NavController's active route.
         *
         * If the NavController is not initialized, the current route is null, or the route
         * cannot be mapped to a known Destination, this returns null.
         *
         * @return The mapped Destination or null when unavailable or unrecognized.
         */
        fun getCurrentDestination(): Destination? {
            val currentRoute = navController?.currentDestination?.route
            return parseRouteToDestination(currentRoute)
        }

        /**
         * Navigate to the manga reading screen for the given manga.
         *
         * @param mangaId The identifier of the manga to open.
         * @param chapterId Optional identifier of the chapter to open; if null, the default or last-read chapter may be used.
         * @param page The page index to open within the chapter (0-based).
         */
        fun navigateToReading(
            mangaId: String,
            chapterId: String? = null,
            page: Int = 0,
        ) {
            navigateTo(Destination.Reading(mangaId, chapterId, page))
        }

        /**
         * Navigate to the watching (video player) screen for a specific anime.
         *
         * @param animeId The ID of the anime to watch.
         * @param episodeId Optional episode ID to open; if null the default/first episode is used.
         * @param timestamp Optional playback position (in milliseconds) to start playback from; defaults to 0.
         */
        fun navigateToWatching(
            animeId: String,
            episodeId: String? = null,
            timestamp: Long = 0,
        ) {
            navigateTo(Destination.Watching(animeId, episodeId, timestamp))
        }

        /**
         * Navigate to the Manga detail screen for a given manga.
         *
         * @param mangaId The unique identifier of the manga to view.
         * @param sourceId Optional identifier of the source to open the manga from (if null, default source is used).
         */
        fun navigateToMangaDetail(
            mangaId: String,
            sourceId: String? = null,
        ) {
            navigateTo(Destination.MangaDetail(mangaId, sourceId))
        }

        /**
         * Navigate to the anime detail screen for the given anime.
         *
         * @param animeId The anime's unique identifier.
         * @param sourceId Optional source identifier to open the anime from a specific source (nullable).
         */
        fun navigateToAnimeDetail(
            animeId: String,
            sourceId: String? = null,
        ) {
            navigateTo(Destination.AnimeDetail(animeId, sourceId))
        }

        /**
         * Navigate to the search screen with optional pre-filled parameters.
         *
         * @param query Initial search query to populate the search field (defaults to empty).
         * @param type Content type to filter search results (defaults to ContentType.ALL).
         * @param source Optional source identifier to scope the search to a specific source.
         */
        fun navigateToSearch(
            query: String = "",
            type: ContentType = ContentType.ALL,
            source: String? = null,
        ) {
            navigateTo(Destination.Search(query, type, source))
        }

        /**
         * Navigate to the app Settings screen.
         *
         * @param section The settings subsection to open (defaults to GENERAL). */
        fun navigateToSettings(section: SettingsSection = SettingsSection.GENERAL) {
            navigateTo(Destination.Settings(section))
        }

        /**
         * Clears the currently stored navigation event so it won't be re-emitted to observers.
         *
         * Call this after handling a navigation event to mark it as consumed.
         */
        fun clearNavigationEvent() {
            _navigationEvents.value = null
        }

        /**
         * Validate and perform navigation to the given destination.
         *
         * Validates the destination using validateNavigation; if valid and a NavController is initialized,
         * navigates to the resolved route using the provided NavOptions. If validation fails the navigation
         * is not attempted. Any exceptions thrown by the navigation call are caught and logged.
         *
         * @param destination The destination to navigate to.
         * @param navOptions Optional navigation options to apply when navigating.
         */
        private fun executeNavigation(
            destination: Destination,
            navOptions: NavOptions?,
        ) {
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
         * Convert a Destination into the navigation route string used by NavController.
         *
         * Maps fixed destinations to their predefined route constants and delegates dynamic destinations
         * (Reading, Watching, MangaDetail, AnimeDetail, Search, Settings) to their respective
         * `createRoute` helpers so embedded parameters are included in the produced route.
         *
         * @return A route string suitable for NavController.navigate.
         */
        private fun getRouteForDestination(destination: Destination): String =
            when (destination) {
                is Destination.Home -> NavigationRoutes.HOME
                is Destination.MangaLibrary -> NavigationRoutes.MANGA_LIBRARY
                is Destination.AnimeLibrary -> NavigationRoutes.ANIME_LIBRARY
                is Destination.Browse -> NavigationRoutes.BROWSE
                is Destination.SourceManagement -> NavigationRoutes.SOURCE_MANAGEMENT
                is Destination.TrackingManagement -> NavigationRoutes.TRACKING_MANAGEMENT
                is Destination.BackupRestore -> NavigationRoutes.BACKUP_RESTORE
                is Destination.AICore -> NavigationRoutes.AI_CORE
                is Destination.EpicDemo -> NavigationRoutes.EPIC_DEMO
                is Destination.Reading ->
                    Destination.Reading.createRoute(
                        destination.mangaId,
                        destination.chapterId,
                        destination.page,
                    )
                is Destination.Watching ->
                    Destination.Watching.createRoute(
                        destination.animeId,
                        destination.episodeId,
                        destination.timestamp,
                    )
                is Destination.MangaDetail ->
                    Destination.MangaDetail.createRoute(
                        destination.mangaId,
                        destination.sourceId,
                    )
                is Destination.AnimeDetail ->
                    Destination.AnimeDetail.createRoute(
                        destination.animeId,
                        destination.sourceId,
                    )
                is Destination.Search ->
                    Destination.Search.createRoute(
                        destination.query,
                        destination.type,
                        destination.source,
                    )
                is Destination.Settings -> Destination.Settings.createRoute(destination.section)
            }

        /**
         * Parse a navigation route string into a Destination instance.
         *
         * Maps well-known static routes (home, libraries, browse, AI core) to their corresponding
         * Destination objects and delegates dynamic routes (reading, watching, manga/anime detail,
         * search, settings) to specialized parser helpers. Returns null for a null or unrecognized route.
         *
         * @param route The navigation route string to parse, or null.
         * @return The corresponding Destination, or null if the route is null or cannot be parsed.
         */
        private fun parseRouteToDestination(route: String?): Destination? =
            when {
                route == null -> null
                route == NavigationRoutes.HOME -> Destination.Home
                route == NavigationRoutes.MANGA_LIBRARY -> Destination.MangaLibrary
                route == NavigationRoutes.ANIME_LIBRARY -> Destination.AnimeLibrary
                route == NavigationRoutes.BROWSE -> Destination.Browse
                route == NavigationRoutes.SOURCE_MANAGEMENT -> Destination.SourceManagement
                route == NavigationRoutes.TRACKING_MANAGEMENT -> Destination.TrackingManagement
                route == NavigationRoutes.BACKUP_RESTORE -> Destination.BackupRestore
                route == NavigationRoutes.AI_CORE -> Destination.AICore
                route == NavigationRoutes.EPIC_DEMO -> Destination.EpicDemo
                route.startsWith("reading/") -> parseReadingRoute(route)
                route.startsWith("watching/") -> parseWatchingRoute(route)
                route.startsWith("manga_detail/") -> parseMangaDetailRoute(route)
                route.startsWith("anime_detail/") -> parseAnimeDetailRoute(route)
                route.startsWith("search") -> parseSearchRoute(route)
                route.startsWith("settings/") -> parseSettingsRoute(route)
                else -> null
            }

        /**
         * Validates that the given destination contains acceptable parameters for navigation.
         *
         * Supported validations:
         * - Destination.Reading: validates `mangaId` and `page`.
         * - Destination.Watching: validates `animeId` and `timestamp`.
         * - Destination.MangaDetail: validates `mangaId`.
         * - Destination.AnimeDetail: validates `animeId`.
         * - Destination.Search: validates `query` and `type`.
         * For any other destination types, no validation is performed and the function returns true.
         *
         * @param destination The destination whose parameters should be validated.
         * @return `true` if the destination's parameters pass validation (or no validation is required); `false` otherwise.
         */
        private fun validateNavigation(destination: Destination): Boolean =
            when (destination) {
                is Destination.Reading ->
                    NavigationValidator.validateMangaId(destination.mangaId) &&
                        NavigationValidator.validatePage(destination.page.toString())
                is Destination.Watching ->
                    NavigationValidator.validateAnimeId(destination.animeId) &&
                        NavigationValidator.validateTimestamp(destination.timestamp.toString())
                is Destination.MangaDetail -> NavigationValidator.validateMangaId(destination.mangaId)
                is Destination.AnimeDetail -> NavigationValidator.validateAnimeId(destination.animeId)
                is Destination.Search ->
                    NavigationValidator.validateSearchQuery(destination.query) &&
                        NavigationValidator.validateContentType(destination.type.name)
                else -> true
            }

        /**
         * Update the internal NavigationState from a route string.
         *
         * Parses the provided route into a Destination and, if successful, updates
         * the backing StateFlow (_navigationState) with:
         * - currentDestination set to the parsed Destination,
         * - previousDestination set to the prior current destination only if it differs (otherwise null),
         * - canNavigateBack reflecting whether back navigation is possible,
         * - navigationHistory appended with the new destination and trimmed to the last 10 entries.
         *
         * @param route The navigation route string to parse; may be null. If parsing fails or the route
         *              is unrecognized, the navigation state is not modified.
         */
        private fun updateNavigationState(route: String?) {
            val currentDestination = parseRouteToDestination(route)
            if (currentDestination != null) {
                val previousDestination = _navigationState.value.currentDestination
                val canNavigateBack = canNavigateBack()
                val navigationHistory =
                    _navigationState.value.navigationHistory.toMutableList().apply {
                        add(currentDestination)
                        if (size > 10) removeAt(0) // Keep only last 10 destinations
                    }

                _navigationState.value =
                    NavigationState(
                        currentDestination = currentDestination,
                        previousDestination =
                            if (currentDestination !=
                                previousDestination
                            ) {
                                previousDestination
                            } else {
                                null
                            },
                        canNavigateBack = canNavigateBack,
                        navigationHistory = navigationHistory,
                    )
            }
        }

        /**
         * Parses a navigation route string and returns a Destination.Reading if the route corresponds to a reading screen.
         *
         * The function extracts expected parameters (e.g., `mangaId`, optional `chapterId`, and optional `page`) from the
         * route and constructs a Destination.Reading. Returns `null` when the route does not match the reading route format
         * or required identifiers are missing.
         *
         * @param route The navigation route string to parse.
         * @return A populated [Destination.Reading] when parsing succeeds, or `null` if the route is not a reading route or is invalid.
         */
        private fun parseReadingRoute(route: String): Destination.Reading? {
            // Pattern: reading/{mangaId}/{chapterId}?page={page} or reading/{mangaId}?page={page}
            if (!route.startsWith("reading/")) return null

            val routeWithoutPrefix = route.removePrefix("reading/")
            val (pathPart, queryPart) =
                if ("?" in routeWithoutPrefix) {
                    val parts = routeWithoutPrefix.split("?", limit = 2)
                    parts[0] to parts[1]
                } else {
                    routeWithoutPrefix to ""
                }

            val pathSegments = pathPart.split("/")
            if (pathSegments.isEmpty()) return null

            val mangaId =
                try {
                    Uri.decode(pathSegments[0])
                } catch (e: Exception) {
                    return null
                }

            val chapterId =
                if (pathSegments.size > 1) {
                    try {
                        Uri.decode(pathSegments[1])
                    } catch (e: Exception) {
                        return null
                    }
                } else {
                    null
                }

            val page =
                if (queryPart.isNotEmpty()) {
                    queryPart
                        .split("&")
                        .find { it.startsWith("page=") }
                        ?.substringAfter("=")
                        ?.toIntOrNull() ?: 0
                } else {
                    0
                }

            return Destination.Reading(mangaId, chapterId, page)
        }

        /**
         * Parses a navigation route string into a [Destination.Watching] instance.
         *
         * The function attempts to recognize a route that represents the "watching" destination and
         * extract any encoded parameters (such as anime/episode identifiers and playback timestamp).
         *
         * @param route The route string to parse.
         * @return A [Destination.Watching] representing the parsed route, or `null` if the route is
         * not a valid watching route or required parameters are missing/unparseable.
         */
        private fun parseWatchingRoute(route: String): Destination.Watching? {
            // Pattern: watching/{animeId}/{episodeId}?timestamp={timestamp} or watching/{animeId}?timestamp={timestamp}
            if (!route.startsWith("watching/")) return null

            val routeWithoutPrefix = route.removePrefix("watching/")
            val (pathPart, queryPart) =
                if ("?" in routeWithoutPrefix) {
                    val parts = routeWithoutPrefix.split("?", limit = 2)
                    parts[0] to parts[1]
                } else {
                    routeWithoutPrefix to ""
                }

            val pathSegments = pathPart.split("/")
            if (pathSegments.isEmpty()) return null

            val animeId =
                try {
                    Uri.decode(pathSegments[0])
                } catch (e: Exception) {
                    return null
                }

            val episodeId =
                if (pathSegments.size > 1) {
                    try {
                        Uri.decode(pathSegments[1])
                    } catch (e: Exception) {
                        return null
                    }
                } else {
                    null
                }

            val timestamp =
                if (queryPart.isNotEmpty()) {
                    queryPart
                        .split("&")
                        .find { it.startsWith("timestamp=") }
                        ?.substringAfter("=")
                        ?.toLongOrNull() ?: 0L
                } else {
                    0L
                }

            return Destination.Watching(animeId, episodeId, timestamp)
        }

        /**
         * Parses a navigation route string into a Destination.MangaDetail instance.
         *
         * Converts a route produced by Destination.MangaDetail.createRoute(...) back into a
         * Destination.MangaDetail with extracted parameters. Returns null if the supplied
         * route is null, unrecognized, or cannot be parsed into a MangaDetail destination.
         *
         * @param route The route string to parse.
         * @return A Destination.MangaDetail when parsing succeeds, or null otherwise.
         */
        private fun parseMangaDetailRoute(route: String): Destination.MangaDetail? {
            // Pattern: manga-detail/{mangaId}/{sourceId} or manga-detail/{mangaId}
            if (!route.startsWith("manga-detail/")) return null

            val pathPart = route.removePrefix("manga-detail/")
            val pathSegments = pathPart.split("/")
            if (pathSegments.isEmpty()) return null

            val mangaId =
                try {
                    Uri.decode(pathSegments[0])
                } catch (e: Exception) {
                    return null
                }

            val sourceId =
                if (pathSegments.size > 1) {
                    try {
                        Uri.decode(pathSegments[1])
                    } catch (e: Exception) {
                        return null
                    }
                } else {
                    null
                }

            return Destination.MangaDetail(mangaId, sourceId)
        }

        /**
         * Parses a navigation route string into a Destination.AnimeDetail if it matches the anime-detail route pattern.
         *
         * The function returns a Destination.AnimeDetail when `route` encodes the anime detail destination (including
         * required and optional path/query parameters); returns null when the route is null, does not match, or cannot
         * be parsed into an AnimeDetail.
         *
         * @param route The navigation route string to parse.
         * @return A Destination.AnimeDetail when parsing succeeds, or null when the route is not an anime-detail route.
         */
        private fun parseAnimeDetailRoute(route: String): Destination.AnimeDetail? {
            // Pattern: anime-detail/{animeId}/{sourceId} or anime-detail/{animeId}
            if (!route.startsWith("anime-detail/")) return null

            val pathPart = route.removePrefix("anime-detail/")
            val pathSegments = pathPart.split("/")
            if (pathSegments.isEmpty()) return null

            val animeId =
                try {
                    Uri.decode(pathSegments[0])
                } catch (e: Exception) {
                    return null
                }

            val sourceId =
                if (pathSegments.size > 1) {
                    try {
                        Uri.decode(pathSegments[1])
                    } catch (e: Exception) {
                        return null
                    }
                } else {
                    null
                }

            return Destination.AnimeDetail(animeId, sourceId)
        }

        /**
         * Parses a navigation route string into a Destination.Search, if possible.
         *
         * Accepts route strings produced by the `Destination.Search` routing logic (the dynamic
         * route format used by the service) and returns a populated `Destination.Search` when the
         * route represents a search screen. Returns `null` for `null`/unrecognized routes.
         *
         * @param route The navigation route string to parse (expected to follow the `Destination.Search` route format).
         * @return A `Destination.Search` built from the route, or `null` if the route does not represent a search destination.
         */
        private fun parseSearchRoute(route: String): Destination.Search? {
            // Pattern: search?query={encodedQuery}&type={TYPE}&source={encodedSource}
            if (!route.startsWith("search")) return null

            val queryPart =
                if ("?" in route) {
                    route.substringAfter("?")
                } else {
                    return Destination.Search() // Return default search with empty parameters
                }

            val queryParams =
                queryPart.split("&").associate { param ->
                    val (key, value) =
                        if ("=" in param) {
                            val parts = param.split("=", limit = 2)
                            parts[0] to parts[1]
                        } else {
                            param to ""
                        }
                    key to value
                }

            val query =
                queryParams["query"]?.let {
                    try {
                        Uri.decode(it)
                    } catch (e: Exception) {
                        ""
                    }
                } ?: ""

            val type =
                queryParams["type"]?.let { typeStr ->
                    try {
                        ContentType.valueOf(typeStr.uppercase())
                    } catch (e: IllegalArgumentException) {
                        ContentType.ALL
                    }
                } ?: ContentType.ALL

            val source =
                queryParams["source"]?.let {
                    try {
                        Uri.decode(it)
                    } catch (e: IllegalArgumentException) {
                        Log.e("NavigationService", "Failed to decode source parameter: $it", e)
                        return null
                    }
                }

            return Destination.Search(query, type, source)
        }

        /**
         * Parses a settings route string into a corresponding [Destination.Settings] instance.
         *
         * The function converts a navigation route (for example a settings screen route with optional
         * section identifiers or parameters) into the typed destination used by the navigation service.
         *
         * @param route The route string to parse.
         * @return The parsed [Destination.Settings] if the route matches the settings route pattern, or `null` if the route is not a settings route or cannot be parsed.
         */
        private fun parseSettingsRoute(route: String): Destination.Settings? {
            // Pattern: settings/{section} where section is lowercase enum name
            if (!route.startsWith("settings")) return null

            return if (route == "settings") {
                // Default settings route
                Destination.Settings()
            } else if (route.startsWith("settings/")) {
                val sectionStr = route.removePrefix("settings/")
                val section =
                    try {
                        SettingsSection.valueOf(sectionStr.uppercase())
                    } catch (e: IllegalArgumentException) {
                        return null // Invalid section name
                    }
                Destination.Settings(section)
            } else {
                null
            }
        }
    }
