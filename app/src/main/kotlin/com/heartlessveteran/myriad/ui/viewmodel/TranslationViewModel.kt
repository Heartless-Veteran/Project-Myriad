package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.services.EnhancedAIService
import com.heartlessveteran.myriad.services.TranslatedTextBound
import com.heartlessveteran.myriad.services.TranslationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Translation state for the reading screen
 */
data class TranslationUiState(
    val isLoading: Boolean = false,
    val translatedTexts: List<TranslatedTextBound> = emptyList(),
    val isTranslationVisible: Boolean = false,
    val error: String? = null,
)

/**
 * ViewModel for managing translation functionality in the reading screen
 */
class TranslationViewModel(
    private val aiService: EnhancedAIService,
) : ViewModel() {
    private val _translationState = MutableStateFlow(TranslationUiState())
    val translationState: StateFlow<TranslationUiState> = _translationState.asStateFlow()

    private val _targetLanguage = MutableStateFlow("en")
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()

    /**
     * Translate the current manga page
     */
    fun translatePage(
        imageBase64: String,
        targetLang: String,
    ) {
        viewModelScope.launch {
            _translationState.value =
                _translationState.value.copy(
                    isLoading = true,
                    error = null,
                )

            try {
                val translationRequest =
                    TranslationRequest(
                        imageBase64 = imageBase64,
                        language = "japanese",
                        targetLanguage = targetLang,
                    )

                when (val result = aiService.translateImageText(imageBase64, translationRequest)) {
                    is Result.Success -> {
                        _translationState.value =
                            _translationState.value.copy(
                                isLoading = false,
                                translatedTexts = result.data.translatedText,
                                isTranslationVisible = true,
                                error = null,
                            )
                    }
                    is Result.Error -> {
                        _translationState.value =
                            _translationState.value.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Translation failed",
                            )
                    }
                    is Result.Loading -> {
                        // Stay in loading state
                    }
                }
            } catch (e: Exception) {
                _translationState.value =
                    _translationState.value.copy(
                        isLoading = false,
                        error = e.message ?: "An unexpected error occurred",
                    )
            }
        }
    }

    /**
     * Toggle translation visibility
     */
    fun toggleTranslationVisibility() {
        _translationState.value =
            _translationState.value.copy(
                isTranslationVisible = !_translationState.value.isTranslationVisible,
            )
    }

    /**
     * Set target language for translation
     */
    fun setTargetLanguage(language: String) {
        _targetLanguage.value = language
    }

    /**
     * Clear current translation
     */
    fun clearTranslation() {
        _translationState.value =
            _translationState.value.copy(
                translatedTexts = emptyList(),
                isTranslationVisible = false,
                error = null,
            )
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _translationState.value = _translationState.value.copy(error = null)
    }

    /**
     * Retry translation with last used parameters
     */
    fun retryTranslation() {
        // This would need to store the last imageBase64 and targetLang
        // For now, just clear the error
        clearError()
    }
}
