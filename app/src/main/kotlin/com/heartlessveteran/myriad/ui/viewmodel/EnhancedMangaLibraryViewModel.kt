package com.heartlessveteran.myriad.ui.viewmodel

import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.entities.MangaStatus
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.repository.MangaRepository
import com.heartlessveteran.myriad.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Enhanced UI State with normalized structure for the Manga Library screen
 */
data class EnhancedMangaLibraryUiState(
    // Data state - normalized structure
    val allManga: Map<String, Manga> = emptyMap(), // ID -> Manga mapping
    val libraryMangaIds: Set<String> = emptySet(), // Only IDs for library manga
    val filteredMangaIds: Set<String> = emptySet(), // Only IDs for filtered results
    // UI state
    val searchQuery: String = "",
    val selectedFilter: MangaFilter = MangaFilter.ALL,
    val sortOrder: SortOrder = SortOrder.TITLE_ASC,
    // Operation states
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncTime: Long? = null,
    // Statistics
    val statistics: LibraryStatistics = LibraryStatistics(),
)

/**
 * Library statistics for analytics
 */
data class LibraryStatistics(
    val totalManga: Int = 0,
    val favoritesCount: Int = 0,
    val readingCount: Int = 0,
    val completedCount: Int = 0,
    val onHoldCount: Int = 0,
    val droppedCount: Int = 0,
)

/*
enum class MangaFilter(val displayName: String) {
    ALL("All Manga"),
    FAVORITES("Favorites"),
    READING("Currently Reading"),
    COMPLETED("Completed"),
    ON_HOLD("On Hold"),
    DROPPED("Dropped"),
    PLAN_TO_READ("Plan to Read")
}
*/

/**
 * Enhanced filter options - Using basic MangaFilter to avoid duplication
 */

/**
 * Sort order options
 */
enum class SortOrder(
    val displayName: String,
) {
    TITLE_ASC("Title A-Z"),
    TITLE_DESC("Title Z-A"),
    DATE_ADDED_DESC("Recently Added"),
    DATE_ADDED_ASC("Oldest First"),
    LAST_READ_DESC("Recently Read"),
    PROGRESS_ASC("Progress Low-High"),
    PROGRESS_DESC("Progress High-Low"),
    RATING_DESC("Highest Rated"),
    RATING_ASC("Lowest Rated"),
}

/**
 * Enhanced ViewModel with normalized state and better error handling
 */
@HiltViewModel
class EnhancedMangaLibraryViewModel
    @Inject
    constructor(
        private val mangaRepository: MangaRepository,
    ) : BaseViewModel<EnhancedMangaLibraryUiState>(EnhancedMangaLibraryUiState()) {
        init {
            initializeLibrary()
        }

        /**
         * Starts collection of repository flows to load and keep the library state up to date.
         *
         * Combines the repository's `getAllManga()` and `getLibraryManga()` flows, builds a normalized
         * map of all manga and a set of library IDs, then updates the ViewModel UI state with:
         * - the normalized `allManga` map,
         * - the current `libraryMangaIds`,
         * - the `filteredMangaIds` computed by applying the current search, filter, and sort,
         * - and recalculated library `statistics`.
         *
         * Errors emitted by the combined flow are delegated to `handleError`.
         */
        private fun initializeLibrary() {
            launchWithErrorHandling {
                // Combine both all manga and library manga flows
                combine(
                    mangaRepository.getAllManga(),
                    mangaRepository.getLibraryManga(),
                ) { allManga, libraryManga ->
                    val allMangaMap = allManga.associateBy { it.id }
                    val libraryIds = libraryManga.map { it.id }.toSet()

                    allMangaMap to libraryIds
                }.catch { exception ->
                    handleError(exception)
                }.collect { (allMangaMap, libraryIds) ->
                    updateUiState { currentState ->
                        val statistics = calculateStatistics(allMangaMap, libraryIds)
                        currentState.copy(
                            allManga = allMangaMap,
                            libraryMangaIds = libraryIds,
                            filteredMangaIds =
                                applyFiltersAndSort(
                                    allMangaMap,
                                    libraryIds,
                                    currentState.searchQuery,
                                    currentState.selectedFilter,
                                    currentState.sortOrder,
                                ),
                            statistics = statistics,
                        )
                    }
                }
            }
        }

        /**
         * Update the current search query and recompute the filtered manga IDs.
         *
         * Recomputes filteredMangaIds by applying the text search to the current
         * allManga and libraryMangaIds using the active selectedFilter and sortOrder,
         * then updates the UI state with the new query and resulting IDs.
         *
         * @param query The search text to apply (case-insensitive matching performed by the filter logic).
         */
        fun searchManga(query: String) {
            updateUiState { currentState ->
                val filteredIds =
                    applyFiltersAndSort(
                        currentState.allManga,
                        currentState.libraryMangaIds,
                        query,
                        currentState.selectedFilter,
                        currentState.sortOrder,
                    )

                currentState.copy(
                    searchQuery = query,
                    filteredMangaIds = filteredIds,
                )
            }
        }

        /**
         * Sets the active library filter and recomputes the set of visible manga IDs.
         *
         * Updates the ViewModel UI state by changing `selectedFilter` and recalculating
         * `filteredMangaIds` using the current library, search query, and sort order.
         *
         * @param filter The MangaFilter to apply (e.g., ALL, FAVORITES, READING).
         */
        fun applyFilter(filter: MangaFilter) {
            updateUiState { currentState ->
                val filteredIds =
                    applyFiltersAndSort(
                        currentState.allManga,
                        currentState.libraryMangaIds,
                        currentState.searchQuery,
                        filter,
                        currentState.sortOrder,
                    )

                currentState.copy(
                    selectedFilter = filter,
                    filteredMangaIds = filteredIds,
                )
            }
        }

        /**
         * Update the current sort order and recompute the visible library list.
         *
         * Replaces the state's `sortOrder` with the provided value and recomputes
         * `filteredMangaIds` using the existing search query and selected filter so the
         * UI reflects the new ordering immediately.
         *
         * @param sortOrder The new sort order to apply.
         */
        fun changeSortOrder(sortOrder: SortOrder) {
            updateUiState { currentState ->
                val filteredIds =
                    applyFiltersAndSort(
                        currentState.allManga,
                        currentState.libraryMangaIds,
                        currentState.searchQuery,
                        currentState.selectedFilter,
                        sortOrder,
                    )

                currentState.copy(
                    sortOrder = sortOrder,
                    filteredMangaIds = filteredIds,
                )
            }
        }

        /**
         * Toggle the favorite flag for a manga with an optimistic UI update.
         *
         * Performs an immediate local flip of the manga's `isFavorite` state to provide a responsive UI,
         * then calls the repository to persist the change. If the repository call fails, the local change
         * is reverted and the error is delegated to the view model's error handler.
         *
         * @param mangaId The ID of the manga to toggle favorite for.
         */
        fun toggleFavorite(mangaId: String) {
            launchWithErrorHandling(showLoading = false) {
                // Optimistic update
                updateUiState { currentState ->
                    val manga = currentState.allManga[mangaId] ?: return@updateUiState currentState
                    val updatedManga = manga.copy(isFavorite = !manga.isFavorite)
                    val updatedMap = currentState.allManga.toMutableMap()
                    updatedMap[mangaId] = updatedManga

                    currentState.copy(allManga = updatedMap)
                }

                // Perform actual update
                when (val result = mangaRepository.toggleFavorite(mangaId)) {
                    is Result.Error -> {
                        // Revert optimistic update
                        updateUiState { currentState ->
                            val manga = currentState.allManga[mangaId] ?: return@updateUiState currentState
                            val revertedManga = manga.copy(isFavorite = !manga.isFavorite)
                            val updatedMap = currentState.allManga.toMutableMap()
                            updatedMap[mangaId] = revertedManga

                            currentState.copy(allManga = updatedMap)
                        }
                        handleError(result.exception)
                    }
                    else -> { /* Success - no action needed */ }
                }
            }
        }

        /**
         * Remove a manga from the user's library.
         *
         * Attempts to remove the manga with the given ID via the repository. This runs asynchronously and does not show a loading indicator.
         * On error the ViewModel's error handler is invoked; on success the UI state is refreshed through the repository flows.
         *
         * @param mangaId The ID of the manga to remove from the library.
         */
        fun removeFromLibrary(mangaId: String) {
            launchWithErrorHandling(showLoading = false) {
                when (val result = mangaRepository.removeFromLibrary(mangaId)) {
                    is Result.Error -> handleError(result.exception)
                    else -> { /* Success - data will update through flow */ }
                }
            }
        }

        /**
         * Refreshes the library by reloading data from the repository.
         *
         * Marks the UI as refreshing while the operation runs, re-initializes the library data,
         * and updates `lastSyncTime` on completion. Any errors are handled by the ViewModel's
         * error-handling mechanism.
         */
        fun refresh() {
            updateUiState { it.copy(isRefreshing = true) }
            launchWithErrorHandling {
                // Re-initialize data
                initializeLibrary()
                updateUiState { it.copy(isRefreshing = false, lastSyncTime = System.currentTimeMillis()) }
            }
        }

        /**
         * Returns the list of Manga objects currently visible in the UI after applying search, filters, and sorting.
         *
         * The order of the returned list follows the ordering of `filteredMangaIds` from the UI state.
         * Any IDs present in `filteredMangaIds` that are missing from `allManga` are skipped.
         *
         * @return A list of Manga corresponding to the current filtered and sorted IDs.
         */
        fun getFilteredManga(): List<Manga> {
            val currentState = currentUiState
            return currentState.filteredMangaIds.mapNotNull { id ->
                currentState.allManga[id]
            }
        }

        /**
         * Retrieve a Manga from the normalized UI state by its unique ID.
         *
         * @param id The manga's unique identifier.
         * @return The corresponding [Manga] if present in the library state, or `null` if not found.
         */
        fun getMangaById(id: String): Manga? = currentUiState.allManga[id]

        /**
         * Filter and sort library manga returning the ordered set of manga IDs.
         *
         * Applies a case-insensitive text search (matches title, author, or description), a category filter
         * (based on the provided MangaFilter), and the requested SortOrder. Manga IDs that are not present
         * in the provided allManga map are ignored. Progress-based sorting treats items with non-positive
         * totalChapters as progress = 0.
         *
         * @param allManga Map of all known Manga keyed by ID.
         * @param libraryIds IDs that represent the current library subset to consider.
         * @param searchQuery Text query to filter by title, author, or description; empty/blank means no text filtering.
         * @param filter Category filter to apply (e.g., FAVORITES, READING, COMPLETED, etc.).
         * @param sortOrder Sort order to apply to the filtered results.
         * @return A Set of manga IDs in the order determined by the applied sorting (iteration order reflects the sort).
         */
        private fun applyFiltersAndSort(
            allManga: Map<String, Manga>,
            libraryIds: Set<String>,
            searchQuery: String,
            filter: MangaFilter,
            sortOrder: SortOrder,
        ): Set<String> {
            val mangaList = libraryIds.mapNotNull { allManga[it] }

            // Apply text search filter
            val searchFiltered =
                if (searchQuery.isBlank()) {
                    mangaList
                } else {
                    mangaList.filter { manga ->
                        manga.title.contains(searchQuery, ignoreCase = true) ||
                            manga.author.contains(searchQuery, ignoreCase = true) ||
                            manga.description.contains(searchQuery, ignoreCase = true)
                    }
                }

            // Apply category filter
            val categoryFiltered =
                when (filter) {
                    MangaFilter.ALL -> searchFiltered
                    MangaFilter.FAVORITES -> searchFiltered.filter { it.isFavorite }
                    MangaFilter.READING ->
                        searchFiltered.filter {
                            it.readChapters > 0 && it.readChapters < it.totalChapters
                        }
                    MangaFilter.COMPLETED ->
                        searchFiltered.filter {
                            it.readChapters >= it.totalChapters && it.totalChapters > 0
                        }
                    MangaFilter.ON_HOLD ->
                        searchFiltered.filter {
                            it.status == MangaStatus.HIATUS
                        }
                    MangaFilter.DROPPED ->
                        searchFiltered.filter {
                            it.status == MangaStatus.CANCELLED
                        }
                    MangaFilter.UNREAD -> searchFiltered.filter { it.readChapters == 0 }
                }

            // Apply sorting
            val sorted =
                when (sortOrder) {
                    SortOrder.TITLE_ASC -> categoryFiltered.sortedBy { it.title }
                    SortOrder.TITLE_DESC -> categoryFiltered.sortedByDescending { it.title }
                    SortOrder.DATE_ADDED_DESC -> categoryFiltered.sortedByDescending { it.dateAdded }
                    SortOrder.DATE_ADDED_ASC -> categoryFiltered.sortedBy { it.dateAdded }
                    SortOrder.LAST_READ_DESC -> categoryFiltered.sortedByDescending { it.lastReadDate }
                    SortOrder.PROGRESS_ASC ->
                        categoryFiltered.sortedBy {
                            if (it.totalChapters > 0) it.readChapters.toFloat() / it.totalChapters else 0f
                        }
                    SortOrder.PROGRESS_DESC ->
                        categoryFiltered.sortedByDescending {
                            if (it.totalChapters > 0) it.readChapters.toFloat() / it.totalChapters else 0f
                        }
                    SortOrder.RATING_DESC -> categoryFiltered.sortedByDescending { it.rating }
                    SortOrder.RATING_ASC -> categoryFiltered.sortedBy { it.rating }
                }

            return sorted.map { it.id }.toSet()
        }

        /**
         * Compute aggregated library statistics for the provided set of manga IDs.
         *
         * Builds the subset of Manga from `libraryIds` using `allManga` and returns counts used for analytics:
         * total manga, favorites, currently reading (in-progress), completed, on hold, and dropped.
         *
         * @param allManga Map of manga ID to Manga objects used to resolve the library entries.
         * @param libraryIds Set of manga IDs representing the current library subset to analyze.
         * @return LibraryStatistics containing counts for totalManga, favoritesCount, readingCount,
         * completedCount, onHoldCount, and droppedCount.
         */
        private fun calculateStatistics(
            allManga: Map<String, Manga>,
            libraryIds: Set<String>,
        ): LibraryStatistics {
            val libraryManga = libraryIds.mapNotNull { allManga[it] }

            return LibraryStatistics(
                totalManga = libraryManga.size,
                favoritesCount = libraryManga.count { it.isFavorite },
                readingCount =
                    libraryManga.count {
                        it.readChapters > 0 && it.readChapters < it.totalChapters
                    },
                completedCount =
                    libraryManga.count {
                        it.readChapters >= it.totalChapters && it.totalChapters > 0
                    },
                onHoldCount = libraryManga.count { it.status == MangaStatus.HIATUS },
                droppedCount = libraryManga.count { it.status == MangaStatus.CANCELLED },
            )
        }
    }
