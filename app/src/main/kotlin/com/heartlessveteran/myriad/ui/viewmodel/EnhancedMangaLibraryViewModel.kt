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
    val statistics: LibraryStatistics = LibraryStatistics()
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
    val droppedCount: Int = 0
)

/**
 * Enhanced filter options
 */
enum class MangaFilter(val displayName: String) {
    ALL("All Manga"),
    FAVORITES("Favorites"),
    READING("Currently Reading"),
    COMPLETED("Completed"),
    ON_HOLD("On Hold"),
    DROPPED("Dropped"),
    UNREAD("Unread")
}

/**
 * Sort order options
 */
enum class SortOrder(val displayName: String) {
    TITLE_ASC("Title A-Z"),
    TITLE_DESC("Title Z-A"),
    DATE_ADDED_DESC("Recently Added"),
    DATE_ADDED_ASC("Oldest First"),
    LAST_READ_DESC("Recently Read"),
    PROGRESS_ASC("Progress Low-High"),
    PROGRESS_DESC("Progress High-Low"),
    RATING_DESC("Highest Rated"),
    RATING_ASC("Lowest Rated")
}

/**
 * Enhanced ViewModel with normalized state and better error handling
 */
@HiltViewModel
class EnhancedMangaLibraryViewModel @Inject constructor(
    private val mangaRepository: MangaRepository
) : BaseViewModel<EnhancedMangaLibraryUiState>(EnhancedMangaLibraryUiState()) {
    
    init {
        initializeLibrary()
    }
    
    /**
     * Initialize library data
     */
    private fun initializeLibrary() {
        launchWithErrorHandling {
            // Combine both all manga and library manga flows
            combine(
                mangaRepository.getAllManga(),
                mangaRepository.getLibraryManga()
            ) { allManga, libraryManga ->
                val allMangaMap = allManga.associateBy { it.id }
                val libraryIds = libraryManga.map { it.id }.toSet()
                
                allMangaMap to libraryIds
            }
            .catch { exception ->
                handleError(exception)
            }
            .collect { (allMangaMap, libraryIds) ->
                updateUiState { currentState ->
                    val statistics = calculateStatistics(allMangaMap, libraryIds)
                    currentState.copy(
                        allManga = allMangaMap,
                        libraryMangaIds = libraryIds,
                        filteredMangaIds = applyFiltersAndSort(
                            allMangaMap,
                            libraryIds,
                            currentState.searchQuery,
                            currentState.selectedFilter,
                            currentState.sortOrder
                        ),
                        statistics = statistics
                    )
                }
            }
        }
    }
    
    /**
     * Search manga with debouncing
     */
    fun searchManga(query: String) {
        updateUiState { currentState ->
            val filteredIds = applyFiltersAndSort(
                currentState.allManga,
                currentState.libraryMangaIds,
                query,
                currentState.selectedFilter,
                currentState.sortOrder
            )
            
            currentState.copy(
                searchQuery = query,
                filteredMangaIds = filteredIds
            )
        }
    }
    
    /**
     * Apply filter to manga list
     */
    fun applyFilter(filter: MangaFilter) {
        updateUiState { currentState ->
            val filteredIds = applyFiltersAndSort(
                currentState.allManga,
                currentState.libraryMangaIds,
                currentState.searchQuery,
                filter,
                currentState.sortOrder
            )
            
            currentState.copy(
                selectedFilter = filter,
                filteredMangaIds = filteredIds
            )
        }
    }
    
    /**
     * Change sort order
     */
    fun changeSortOrder(sortOrder: SortOrder) {
        updateUiState { currentState ->
            val filteredIds = applyFiltersAndSort(
                currentState.allManga,
                currentState.libraryMangaIds,
                currentState.searchQuery,
                currentState.selectedFilter,
                sortOrder
            )
            
            currentState.copy(
                sortOrder = sortOrder,
                filteredMangaIds = filteredIds
            )
        }
    }
    
    /**
     * Toggle favorite status with optimistic updates
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
     * Remove manga from library with confirmation
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
     * Refresh library data
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
     * Get filtered manga list for UI
     */
    fun getFilteredManga(): List<Manga> {
        val currentState = currentUiState
        return currentState.filteredMangaIds.mapNotNull { id ->
            currentState.allManga[id]
        }
    }
    
    /**
     * Get manga by ID
     */
    fun getMangaById(id: String): Manga? {
        return currentUiState.allManga[id]
    }
    
    /**
     * Apply filters and sorting with normalized state
     */
    private fun applyFiltersAndSort(
        allManga: Map<String, Manga>,
        libraryIds: Set<String>,
        searchQuery: String,
        filter: MangaFilter,
        sortOrder: SortOrder
    ): Set<String> {
        val mangaList = libraryIds.mapNotNull { allManga[it] }
        
        // Apply text search filter
        val searchFiltered = if (searchQuery.isBlank()) {
            mangaList
        } else {
            mangaList.filter { manga ->
                manga.title.contains(searchQuery, ignoreCase = true) ||
                manga.author.contains(searchQuery, ignoreCase = true) ||
                manga.description.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Apply category filter
        val categoryFiltered = when (filter) {
            MangaFilter.ALL -> searchFiltered
            MangaFilter.FAVORITES -> searchFiltered.filter { it.isFavorite }
            MangaFilter.READING -> searchFiltered.filter { 
                it.readChapters > 0 && it.readChapters < it.totalChapters 
            }
            MangaFilter.COMPLETED -> searchFiltered.filter { 
                it.readChapters >= it.totalChapters && it.totalChapters > 0 
            }
            MangaFilter.ON_HOLD -> searchFiltered.filter { 
                it.status == MangaStatus.HIATUS 
            }
            MangaFilter.DROPPED -> searchFiltered.filter { 
                it.status == MangaStatus.CANCELLED 
            }
            MangaFilter.UNREAD -> searchFiltered.filter { it.readChapters == 0 }
        }
        
        // Apply sorting
        val sorted = when (sortOrder) {
            SortOrder.TITLE_ASC -> categoryFiltered.sortedBy { it.title }
            SortOrder.TITLE_DESC -> categoryFiltered.sortedByDescending { it.title }
            SortOrder.DATE_ADDED_DESC -> categoryFiltered.sortedByDescending { it.dateAdded }
            SortOrder.DATE_ADDED_ASC -> categoryFiltered.sortedBy { it.dateAdded }
            SortOrder.LAST_READ_DESC -> categoryFiltered.sortedByDescending { it.lastReadDate }
            SortOrder.PROGRESS_ASC -> categoryFiltered.sortedBy { 
                if (it.totalChapters > 0) it.readChapters.toFloat() / it.totalChapters else 0f 
            }
            SortOrder.PROGRESS_DESC -> categoryFiltered.sortedByDescending { 
                if (it.totalChapters > 0) it.readChapters.toFloat() / it.totalChapters else 0f 
            }
            SortOrder.RATING_DESC -> categoryFiltered.sortedByDescending { it.rating }
            SortOrder.RATING_ASC -> categoryFiltered.sortedBy { it.rating }
        }
        
        return sorted.map { it.id }.toSet()
    }
    
    /**
     * Calculate library statistics
     */
    private fun calculateStatistics(
        allManga: Map<String, Manga>,
        libraryIds: Set<String>
    ): LibraryStatistics {
        val libraryManga = libraryIds.mapNotNull { allManga[it] }
        
        return LibraryStatistics(
            totalManga = libraryManga.size,
            favoritesCount = libraryManga.count { it.isFavorite },
            readingCount = libraryManga.count { 
                it.readChapters > 0 && it.readChapters < it.totalChapters 
            },
            completedCount = libraryManga.count { 
                it.readChapters >= it.totalChapters && it.totalChapters > 0 
            },
            onHoldCount = libraryManga.count { it.status == MangaStatus.HIATUS },
            droppedCount = libraryManga.count { it.status == MangaStatus.CANCELLED }
        )
    }
}