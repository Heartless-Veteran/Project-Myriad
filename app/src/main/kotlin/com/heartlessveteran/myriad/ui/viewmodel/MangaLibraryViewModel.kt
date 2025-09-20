package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.entities.MangaStatus
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.repository.MangaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * UI State for the Manga Library screen
 */
data class MangaLibraryUiState(
    val isLoading: Boolean = false,
    val mangaList: List<Manga> = emptyList(),
    val filteredMangaList: List<Manga> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: MangaFilter = MangaFilter.ALL,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,
    // Phase 1 Enhancement: Library statistics
    val totalCount: Int = 0,
    val favoriteCount: Int = 0,
    val readingCount: Int = 0,
    val completedCount: Int = 0,
)

/**
 * Filter options for manga library
 */
enum class MangaFilter {
    ALL,
    FAVORITES,
    READING,
    COMPLETED,
    ON_HOLD,
    DROPPED,
    UNREAD,
}

/**
 * Statistics data for manga library
 */
data class MangaLibraryStatistics(
    val totalCount: Int,
    val favoriteCount: Int,
    val readingCount: Int,
    val completedCount: Int,
)

/**
 * ViewModel for managing manga library state and operations
 */
class MangaLibraryViewModel(
    private val mangaRepository: MangaRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MangaLibraryUiState())
    val uiState: StateFlow<MangaLibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibraryManga()
    }

    /**
     * Load manga from the library
     */
    fun loadLibraryManga() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            mangaRepository
                .getLibraryManga()
                .catch { exception ->
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to load library: ${exception.message}",
                        )
                }.collect { mangaList ->
                    val statistics = calculateStatistics(mangaList)
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            mangaList = mangaList,
                            filteredMangaList = applyFilters(mangaList),
                            totalCount = statistics.totalCount,
                            favoriteCount = statistics.favoriteCount,
                            readingCount = statistics.readingCount,
                            completedCount = statistics.completedCount,
                        )
                }
        }
    }

    /**
     * Search manga in the library
     */
    fun searchManga(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.value =
                    _uiState.value.copy(
                        filteredMangaList = applyFilters(_uiState.value.mangaList),
                    )
            } else {
                mangaRepository
                    .searchManga(query)
                    .catch { exception ->
                        _uiState.value =
                            _uiState.value.copy(
                                errorMessage = "Search failed: ${exception.message}",
                            )
                    }.collect { results ->
                        _uiState.value =
                            _uiState.value.copy(
                                filteredMangaList = applyFilters(results),
                            )
                    }
            }
        }
    }

    /**
     * Apply selected filter to manga list
     */
    fun applyFilter(filter: MangaFilter) {
        _uiState.value =
            _uiState.value.copy(
                selectedFilter = filter,
                filteredMangaList = applyFilters(_uiState.value.mangaList),
            )
    }

    /**
     * Toggle favorite status of a manga
     */
    fun toggleFavorite(mangaId: String) {
        viewModelScope.launch {
            mangaRepository.toggleFavorite(mangaId).let { result ->
                when (result) {
                    is Result.Error -> {
                        _uiState.value =
                            _uiState.value.copy(
                                errorMessage = "Failed to update favorite: ${result.message}",
                            )
                    }
                    is Result.Success -> {
                        // Library will automatically update through Flow
                    }
                    is Result.Loading -> { /* No action needed */ }
                }
            }
        }
    }

    /**
     * Remove manga from library
     */
    fun removeFromLibrary(mangaId: String) {
        viewModelScope.launch {
            mangaRepository.removeFromLibrary(mangaId).let { result ->
                when (result) {
                    is Result.Error -> {
                        _uiState.value =
                            _uiState.value.copy(
                                errorMessage = "Failed to remove from library: ${result.message}",
                            )
                    }
                    is Result.Success -> {
                        // Library will automatically update through Flow
                    }
                    is Result.Loading -> { /* No action needed */ }
                }
            }
        }
    }

    /**
     * Refresh the library
     */
    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadLibraryManga()
        _uiState.value = _uiState.value.copy(isRefreshing = false)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Apply filters based on current selection
     */
    private fun applyFilters(mangaList: List<Manga>): List<Manga> =
        when (_uiState.value.selectedFilter) {
            MangaFilter.ALL -> mangaList
            MangaFilter.FAVORITES -> mangaList.filter { it.isFavorite }
            MangaFilter.READING ->
                mangaList.filter {
                    it.readChapters > 0 && it.readChapters < it.totalChapters
                }
            MangaFilter.COMPLETED ->
                mangaList.filter {
                    it.readChapters >= it.totalChapters && it.totalChapters > 0
                }
            MangaFilter.ON_HOLD ->
                mangaList.filter {
                    it.status == MangaStatus.HIATUS
                }
            MangaFilter.DROPPED ->
                mangaList.filter {
                    it.status == MangaStatus.CANCELLED
                }
            MangaFilter.UNREAD ->
                mangaList.filter {
                    it.readChapters == 0
                }
        }

    /**
     * Calculate library statistics for display
     */
    private fun calculateStatistics(mangaList: List<Manga>): MangaLibraryStatistics =
        MangaLibraryStatistics(
            totalCount = mangaList.size,
            favoriteCount = mangaList.count { it.isFavorite },
            readingCount =
                mangaList.count {
                    it.readChapters > 0 &&
                        it.totalChapters > 0 &&
                        it.readChapters < it.totalChapters
                },
            completedCount = mangaList.count { it.totalChapters > 0 && it.readChapters >= it.totalChapters },
        )
}
