package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.core.domain.entities.BackgroundColor
import com.heartlessveteran.myriad.core.domain.entities.PageLayout
import com.heartlessveteran.myriad.core.domain.entities.ReadingDirection
import com.heartlessveteran.myriad.core.domain.entities.ReaderSettings
import com.heartlessveteran.myriad.core.domain.entities.ZoomType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Temporary Reader Settings ViewModel without Hilt dependencies
 */
class TempReaderSettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderSettingsUiState())
    val uiState: StateFlow<ReaderSettingsUiState> = _uiState.asStateFlow()

    private val _uiEvents = Channel<ReaderSettingsUiEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()

    // Simple state for reader settings
    private val _readerSettings = MutableStateFlow(ReaderSettings())
    val readerSettings: StateFlow<ReaderSettings> = _readerSettings.asStateFlow()

    fun onEvent(event: ReaderSettingsEvent) {
        when (event) {
            is ReaderSettingsEvent.UpdateReadingDirection -> updateReadingDirection(event.direction)
            is ReaderSettingsEvent.UpdatePageLayout -> updatePageLayout(event.layout)
            is ReaderSettingsEvent.UpdateZoomType -> updateZoomType(event.zoomType)
            is ReaderSettingsEvent.UpdateBackgroundColor -> updateBackgroundColor(event.color)
            is ReaderSettingsEvent.UpdateKeepScreenOn -> updateKeepScreenOn(event.enabled)
            is ReaderSettingsEvent.ResetToDefaults -> resetSettings()
            else -> {} // Handle other events
        }
    }

    private fun updateReadingDirection(direction: ReadingDirection) {
        _readerSettings.value = _readerSettings.value.copy(readingDirection = direction)
    }

    private fun updatePageLayout(layout: PageLayout) {
        _readerSettings.value = _readerSettings.value.copy(pageLayout = layout)
    }

    private fun updateZoomType(zoomType: ZoomType) {
        _readerSettings.value = _readerSettings.value.copy(zoomType = zoomType)
    }

    private fun updateBackgroundColor(color: BackgroundColor) {
        _readerSettings.value = _readerSettings.value.copy(backgroundColor = color)
    }

    private fun updateKeepScreenOn(enabled: Boolean) {
        _readerSettings.value = _readerSettings.value.copy(keepScreenOn = enabled)
    }

    private fun resetSettings() {
        _readerSettings.value = ReaderSettings()
        viewModelScope.launch {
            _uiEvents.send(ReaderSettingsUiEvent.SettingsReset)
        }
    }
}