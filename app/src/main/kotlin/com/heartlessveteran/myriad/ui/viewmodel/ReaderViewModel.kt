package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.usecase.GetChapterPagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the reader screen
 */
data class ReaderUiState(
    val pages: List<String> = emptyList(),
    val currentPage: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val readingDirection: ReadingDirection = ReadingDirection.LEFT_TO_RIGHT,
    val zoomLevel: Float = 1f,
    val isFullscreen: Boolean = false,
)

/**
 * Reading direction options
 */
enum class ReadingDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    VERTICAL,
}

/**
 * Events that can be sent from the UI
 */
sealed class ReaderEvent {
    data class LoadChapter(
        val sourceId: String,
        val chapterUrl: String,
    ) : ReaderEvent()

    data class GoToPage(
        val pageIndex: Int,
    ) : ReaderEvent()

    data object NextPage : ReaderEvent()

    data object PreviousPage : ReaderEvent()

    data class ChangeReadingDirection(
        val direction: ReadingDirection,
    ) : ReaderEvent()

    data class ChangeZoom(
        val zoomLevel: Float,
    ) : ReaderEvent()

    data object ToggleFullscreen : ReaderEvent()

    data object ClearError : ReaderEvent()
}

/**
 * One-time events for the UI
 */
sealed class ReaderUiEvent {
    data class ShowError(
        val message: String,
    ) : ReaderUiEvent()

    data object ChapterCompleted : ReaderUiEvent()

    data object NavigateBack : ReaderUiEvent()
}

/**
 * ViewModel for the manga reader screen.
 * Follows MVVM architecture and handles chapter page loading and navigation.
 * Demonstrates usage of GetChapterPagesUseCase as per architecture requirements.
 * Uses Hilt dependency injection.
 */
@HiltViewModel
class ReaderViewModel
    @Inject
    constructor(
        private val getChapterPagesUseCase: GetChapterPagesUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ReaderUiState())
        val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

        private val _uiEvents = Channel<ReaderUiEvent>()
        val uiEvents = _uiEvents.receiveAsFlow()

        /**
         * Handles events from the UI
         */
        fun onEvent(event: ReaderEvent) {
            when (event) {
                is ReaderEvent.LoadChapter -> {
                    loadChapterPages(event.sourceId, event.chapterUrl)
                }
                is ReaderEvent.GoToPage -> {
                    goToPage(event.pageIndex)
                }
                ReaderEvent.NextPage -> {
                    nextPage()
                }
                ReaderEvent.PreviousPage -> {
                    previousPage()
                }
                is ReaderEvent.ChangeReadingDirection -> {
                    _uiState.value = _uiState.value.copy(readingDirection = event.direction)
                }
                is ReaderEvent.ChangeZoom -> {
                    _uiState.value = _uiState.value.copy(zoomLevel = event.zoomLevel)
                }
                ReaderEvent.ToggleFullscreen -> {
                    _uiState.value = _uiState.value.copy(isFullscreen = !_uiState.value.isFullscreen)
                }
                ReaderEvent.ClearError -> {
                    _uiState.value = _uiState.value.copy(errorMessage = null)
                }
            }
        }

        /**
         * Loads chapter pages using the use case
         */
        private fun loadChapterPages(
            sourceId: String,
            chapterUrl: String,
        ) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                when (val result = getChapterPagesUseCase(sourceId, chapterUrl)) {
                    is Result.Success -> {
                        _uiState.value =
                            _uiState.value.copy(
                                pages = result.data,
                                currentPage = 0,
                                isLoading = false,
                            )
                    }
                    is Result.Error -> {
                        val errorMessage = result.message ?: "Failed to load chapter pages"
                        _uiState.value =
                            _uiState.value.copy(
                                isLoading = false,
                                errorMessage = errorMessage,
                            )
                        _uiEvents.trySend(ReaderUiEvent.ShowError(errorMessage))
                    }
                    is Result.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }

        /**
         * Navigates to a specific page
         */
        private fun goToPage(pageIndex: Int) {
            val currentState = _uiState.value
            if (pageIndex in 0 until currentState.pages.size) {
                _uiState.value = currentState.copy(currentPage = pageIndex)
            }
        }

        /**
         * Navigates to the next page
         */
        private fun nextPage() {
            val currentState = _uiState.value
            val nextIndex =
                when (currentState.readingDirection) {
                    ReadingDirection.LEFT_TO_RIGHT, ReadingDirection.VERTICAL -> currentState.currentPage + 1
                    ReadingDirection.RIGHT_TO_LEFT -> currentState.currentPage - 1
                }

            if (nextIndex >= currentState.pages.size) {
                // Chapter completed
                _uiEvents.trySend(ReaderUiEvent.ChapterCompleted)
            } else if (nextIndex >= 0) {
                _uiState.value = currentState.copy(currentPage = nextIndex)
            }
        }

        /**
         * Navigates to the previous page
         */
        private fun previousPage() {
            val currentState = _uiState.value
            val prevIndex =
                when (currentState.readingDirection) {
                    ReadingDirection.LEFT_TO_RIGHT, ReadingDirection.VERTICAL -> currentState.currentPage - 1
                    ReadingDirection.RIGHT_TO_LEFT -> currentState.currentPage + 1
                }

            if (prevIndex < 0) {
                // At the beginning of chapter
                return
            } else if (prevIndex < currentState.pages.size) {
                _uiState.value = currentState.copy(currentPage = prevIndex)
            }
        }

        /**
         * Gets the current page URL for display
         */
        fun getCurrentPageUrl(): String? {
            val currentState = _uiState.value
            return if (currentState.currentPage in 0 until currentState.pages.size) {
                currentState.pages[currentState.currentPage]
            } else {
                null
            }
        }

        /**
         * Gets the reading progress as a percentage
         */
        fun getReadingProgress(): Float {
            val currentState = _uiState.value
            return if (currentState.pages.isNotEmpty()) {
                (currentState.currentPage + 1).toFloat() / currentState.pages.size.toFloat()
            } else {
                0f
            }
        }
    }
