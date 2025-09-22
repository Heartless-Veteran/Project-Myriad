package com.heartlessveteran.myriad.feature.vault.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.feature.vault.domain.usecase.GetReadingProgressUseCase
import com.heartlessveteran.myriad.feature.vault.domain.usecase.ReadingProgress
import com.heartlessveteran.myriad.feature.vault.domain.usecase.UpdateReadingProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Enhanced ViewModel for the manga reader with progress tracking and reading modes.
 */
class MangaReaderViewModel(
    private val updateProgressUseCase: UpdateReadingProgressUseCase,
    private val getProgressUseCase: GetReadingProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MangaReaderUiState())
    val uiState: StateFlow<MangaReaderUiState> = _uiState.asStateFlow()

    private val _readingProgress = MutableStateFlow<ReadingProgress?>(null)
    val readingProgress: StateFlow<ReadingProgress?> = _readingProgress.asStateFlow()

    /**
     * Initialize the reader with manga and chapter data
     */
    fun initializeReader(
        mangaId: String,
        chapterId: String,
        mangaTitle: String,
        chapterTitle: String,
        pages: List<String>
    ) {
        _uiState.value = _uiState.value.copy(
            mangaId = mangaId,
            chapterId = chapterId,
            mangaTitle = mangaTitle,
            chapterTitle = chapterTitle,
            pages = pages,
            totalPages = pages.size,
            isLoading = false
        )
        
        // Load existing progress
        loadReadingProgress(chapterId)
    }

    /**
     * Load reading progress from database
     */
    private fun loadReadingProgress(chapterId: String) {
        viewModelScope.launch {
            getProgressUseCase(chapterId).fold(
                onSuccess = { progress ->
                    _readingProgress.value = progress
                    _uiState.value = _uiState.value.copy(
                        currentPage = progress.currentPage
                    )
                },
                onError = { _, _ ->
                    // Progress not found, start from beginning
                    _readingProgress.value = null
                }
            )
        }
    }

    /**
     * Update current page and save progress
     */
    fun updateCurrentPage(page: Int) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(currentPage = page)
        
        // Save progress to database
        viewModelScope.launch {
            updateProgressUseCase(
                mangaId = currentState.mangaId,
                chapterId = currentState.chapterId,
                currentPage = page,
                totalPages = currentState.totalPages,
                isCompleted = page >= currentState.totalPages - 1
            )
        }
    }

    /**
     * Change reading mode
     */
    fun changeReadingMode(mode: ReadingMode) {
        _uiState.value = _uiState.value.copy(readingMode = mode)
    }

    /**
     * Change reading direction
     */
    fun changeReadingDirection(direction: ReadingDirection) {
        _uiState.value = _uiState.value.copy(readingDirection = direction)
    }

    /**
     * Toggle UI visibility
     */
    fun toggleUIVisibility() {
        _uiState.value = _uiState.value.copy(
            isUIVisible = !_uiState.value.isUIVisible
        )
    }

    /**
     * Set zoom level
     */
    fun setZoomLevel(zoom: Float) {
        _uiState.value = _uiState.value.copy(zoomLevel = zoom.coerceIn(0.5f, 3f))
    }

    /**
     * Go to next page
     */
    fun nextPage() {
        val currentState = _uiState.value
        if (currentState.currentPage < currentState.totalPages - 1) {
            updateCurrentPage(currentState.currentPage + 1)
        }
    }

    /**
     * Go to previous page
     */
    fun previousPage() {
        val currentState = _uiState.value
        if (currentState.currentPage > 0) {
            updateCurrentPage(currentState.currentPage - 1)
        }
    }

    /**
     * Mark chapter as completed
     */
    fun completeChapter() {
        val currentState = _uiState.value
        viewModelScope.launch {
            updateProgressUseCase(
                mangaId = currentState.mangaId,
                chapterId = currentState.chapterId,
                currentPage = currentState.totalPages - 1,
                totalPages = currentState.totalPages,
                isCompleted = true
            )
        }
    }
}

/**
 * UI state for the manga reader
 */
data class MangaReaderUiState(
    val mangaId: String = "",
    val chapterId: String = "",
    val mangaTitle: String = "",
    val chapterTitle: String = "",
    val pages: List<String> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val readingMode: ReadingMode = ReadingMode.SINGLE_PAGE,
    val readingDirection: ReadingDirection = ReadingDirection.LEFT_TO_RIGHT,
    val isUIVisible: Boolean = false,
    val zoomLevel: Float = 1f,
    val isLoading: Boolean = true
)

/**
 * Different reading modes supported by the reader
 */
enum class ReadingMode {
    SINGLE_PAGE,
    DOUBLE_PAGE,
    CONTINUOUS_VERTICAL,
    WEBTOON
}

/**
 * Reading direction options
 */
enum class ReadingDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    VERTICAL
}