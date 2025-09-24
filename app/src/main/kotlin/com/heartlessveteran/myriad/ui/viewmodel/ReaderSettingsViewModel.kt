package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.core.domain.entities.BackgroundColor
import com.heartlessveteran.myriad.core.domain.entities.PageLayout
import com.heartlessveteran.myriad.core.domain.entities.ReadingDirection
import com.heartlessveteran.myriad.core.domain.entities.ReaderSettings
import com.heartlessveteran.myriad.core.domain.entities.ZoomType
import com.heartlessveteran.myriad.core.domain.model.Result
import com.heartlessveteran.myriad.core.domain.usecase.GetReaderSettingsUseCase
import com.heartlessveteran.myriad.core.domain.usecase.SaveReaderSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for reader settings screen
 */
data class ReaderSettingsUiState(
    val settings: ReaderSettings = ReaderSettings(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * Events for reader settings
 */
sealed class ReaderSettingsEvent {
    data class UpdateReadingDirection(val direction: ReadingDirection) : ReaderSettingsEvent()
    data class UpdatePageLayout(val layout: PageLayout) : ReaderSettingsEvent()
    data class UpdateBackgroundColor(val color: BackgroundColor) : ReaderSettingsEvent()
    data class UpdateZoomType(val zoomType: ZoomType) : ReaderSettingsEvent()
    data class UpdateCustomZoom(val zoomLevel: Float) : ReaderSettingsEvent()
    data class UpdatePageSpacing(val spacing: Int) : ReaderSettingsEvent()
    data class UpdateVolumeKeyNavigation(val enabled: Boolean) : ReaderSettingsEvent()
    data class UpdateTapNavigation(val enabled: Boolean) : ReaderSettingsEvent()
    data class UpdateFullscreenMode(val enabled: Boolean) : ReaderSettingsEvent()
    data class UpdateKeepScreenOn(val enabled: Boolean) : ReaderSettingsEvent()
    data class UpdateShowPageIndicator(val enabled: Boolean) : ReaderSettingsEvent()
    data class UpdateDoubleTapZoom(val enabled: Boolean) : ReaderSettingsEvent()
    data class UpdateCropBorders(val enabled: Boolean) : ReaderSettingsEvent()
    data class UpdateAnimationDuration(val duration: Int) : ReaderSettingsEvent()
    data object ResetToDefaults : ReaderSettingsEvent()
    data object ClearError : ReaderSettingsEvent()
}

/**
 * One-time UI events
 */
sealed class ReaderSettingsUiEvent {
    data class ShowError(val message: String) : ReaderSettingsUiEvent()
    data object SettingsSaved : ReaderSettingsUiEvent()
    data object SettingsReset : ReaderSettingsUiEvent()
}

/**
 * ViewModel for reader settings screen.
 * Manages reader configuration and preferences.
 * Follows MVVM architecture with StateFlow for reactive UI updates.
 */
@HiltViewModel
class ReaderSettingsViewModel @Inject constructor(
    private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    private val saveReaderSettingsUseCase: SaveReaderSettingsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderSettingsUiState())
    val uiState: StateFlow<ReaderSettingsUiState> = _uiState.asStateFlow()

    private val _uiEvents = Channel<ReaderSettingsUiEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()

    // Observe reader settings changes
    val readerSettings = getReaderSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReaderSettings()
        )

    init {
        // Update UI state when settings change
        viewModelScope.launch {
            readerSettings.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Handle events from the UI
     */
    fun onEvent(event: ReaderSettingsEvent) {
        when (event) {
            is ReaderSettingsEvent.UpdateReadingDirection -> {
                updateSettings { it.copy(readingDirection = event.direction) }
            }
            is ReaderSettingsEvent.UpdatePageLayout -> {
                updateSettings { it.copy(pageLayout = event.layout) }
            }
            is ReaderSettingsEvent.UpdateBackgroundColor -> {
                updateSettings { it.copy(backgroundColor = event.color) }
            }
            is ReaderSettingsEvent.UpdateZoomType -> {
                updateSettings { it.copy(zoomType = event.zoomType) }
            }
            is ReaderSettingsEvent.UpdateCustomZoom -> {
                updateSettings { it.copy(customZoomLevel = event.zoomLevel) }
            }
            is ReaderSettingsEvent.UpdatePageSpacing -> {
                updateSettings { it.copy(pageSpacing = event.spacing) }
            }
            is ReaderSettingsEvent.UpdateVolumeKeyNavigation -> {
                updateSettings { it.copy(enableVolumeKeyNavigation = event.enabled) }
            }
            is ReaderSettingsEvent.UpdateTapNavigation -> {
                updateSettings { it.copy(enableTapNavigation = event.enabled) }
            }
            is ReaderSettingsEvent.UpdateFullscreenMode -> {
                updateSettings { it.copy(fullscreenMode = event.enabled) }
            }
            is ReaderSettingsEvent.UpdateKeepScreenOn -> {
                updateSettings { it.copy(keepScreenOn = event.enabled) }
            }
            is ReaderSettingsEvent.UpdateShowPageIndicator -> {
                updateSettings { it.copy(showPageIndicator = event.enabled) }
            }
            is ReaderSettingsEvent.UpdateDoubleTapZoom -> {
                updateSettings { it.copy(enableDoubleTapZoom = event.enabled) }
            }
            is ReaderSettingsEvent.UpdateCropBorders -> {
                updateSettings { it.copy(cropBorders = event.enabled) }
            }
            is ReaderSettingsEvent.UpdateAnimationDuration -> {
                updateSettings { it.copy(animationDuration = event.duration) }
            }
            is ReaderSettingsEvent.ResetToDefaults -> {
                resetToDefaults()
            }
            is ReaderSettingsEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
        }
    }

    /**
     * Update settings with a transformation function
     */
    private fun updateSettings(transform: (ReaderSettings) -> ReaderSettings) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val currentSettings = _uiState.value.settings
                val updatedSettings = transform(currentSettings)
                
                when (val result = saveReaderSettingsUseCase(updatedSettings)) {
                    is Result.Success -> {
                        _uiEvents.trySend(ReaderSettingsUiEvent.SettingsSaved)
                    }
                    is Result.Error -> {
                        val errorMessage = result.message ?: "Failed to save settings"
                        _uiState.value = _uiState.value.copy(
                            errorMessage = errorMessage,
                            isLoading = false
                        )
                        _uiEvents.trySend(ReaderSettingsUiEvent.ShowError(errorMessage))
                    }
                    is Result.Loading -> {
                        // Already loading
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "Failed to update settings: ${e.message}"
                _uiState.value = _uiState.value.copy(
                    errorMessage = errorMessage,
                    isLoading = false
                )
                _uiEvents.trySend(ReaderSettingsUiEvent.ShowError(errorMessage))
            }
        }
    }

    /**
     * Reset settings to default values
     */
    private fun resetToDefaults() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val defaultSettings = ReaderSettings()
                
                when (val result = saveReaderSettingsUseCase(defaultSettings)) {
                    is Result.Success -> {
                        _uiEvents.trySend(ReaderSettingsUiEvent.SettingsReset)
                    }
                    is Result.Error -> {
                        val errorMessage = result.message ?: "Failed to reset settings"
                        _uiState.value = _uiState.value.copy(
                            errorMessage = errorMessage,
                            isLoading = false
                        )
                        _uiEvents.trySend(ReaderSettingsUiEvent.ShowError(errorMessage))
                    }
                    is Result.Loading -> {
                        // Already loading
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "Failed to reset settings: ${e.message}"
                _uiState.value = _uiState.value.copy(
                    errorMessage = errorMessage,
                    isLoading = false
                )
                _uiEvents.trySend(ReaderSettingsUiEvent.ShowError(errorMessage))
            }
        }
    }
}