package com.heartlessveteran.myriad.feature.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heartlessveteran.myriad.core.data.ai.AIProviderRegistry
import com.heartlessveteran.myriad.core.domain.ai.AIProvider
import com.heartlessveteran.myriad.core.domain.model.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing AI provider selection and operations.
 * Follows the project's MVVM pattern with StateFlow for UI state management.
 */
class AIViewModel(
    private val aiProviderRegistry: AIProviderRegistry
) : ViewModel() {

    private val _currentProvider = MutableStateFlow<AIProvider>(
        aiProviderRegistry.getDefaultProvider()
    )
    val currentProvider: StateFlow<AIProvider> = _currentProvider.asStateFlow()

    private val _availableProviders = MutableStateFlow<List<AIProvider>>(
        aiProviderRegistry.getProviders()
    )
    val availableProviders: StateFlow<List<AIProvider>> = _availableProviders.asStateFlow()

    private val _aiOperationState = MutableStateFlow<AIOperationState>(AIOperationState.Idle)
    val aiOperationState: StateFlow<AIOperationState> = _aiOperationState.asStateFlow()

    /**
     * Select a new AI provider
     */
    fun selectProvider(provider: AIProvider) {
        _currentProvider.value = provider
    }

    /**
     * Generate recommendations using the current provider
     */
    fun generateRecommendations(
        userPreferences: Map<String, Any>,
        readingHistory: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _aiOperationState.value = AIOperationState.Loading
            
            val result = _currentProvider.value.generateRecommendations(userPreferences, readingHistory)
            
            _aiOperationState.value = when (result) {
                is Result.Success -> AIOperationState.Success(result.data)
                is Result.Error -> AIOperationState.Error(result.exception.message ?: "Unknown error")
                is Result.Loading -> AIOperationState.Loading
            }
        }
    }

    /**
     * Analyze art style using the current provider
     */
    fun analyzeArtStyle(imageData: ByteArray) {
        viewModelScope.launch {
            _aiOperationState.value = AIOperationState.Loading
            
            val result = _currentProvider.value.analyzeArtStyle(imageData)
            
            _aiOperationState.value = when (result) {
                is Result.Success -> AIOperationState.Success(result.data)
                is Result.Error -> AIOperationState.Error(result.exception.message ?: "Unknown error")
                is Result.Loading -> AIOperationState.Loading
            }
        }
    }

    /**
     * Translate text using the current provider
     */
    fun translateText(imageData: ByteArray) {
        viewModelScope.launch {
            _aiOperationState.value = AIOperationState.Loading
            
            val result = _currentProvider.value.translateText(imageData)
            
            _aiOperationState.value = when (result) {
                is Result.Success -> AIOperationState.Success(result.data)
                is Result.Error -> AIOperationState.Error(result.exception.message ?: "Unknown error")
                is Result.Loading -> AIOperationState.Loading
            }
        }
    }

    /**
     * Clear the current AI operation state
     */
    fun clearState() {
        _aiOperationState.value = AIOperationState.Idle
    }
}

/**
 * Represents the state of AI operations for UI feedback
 */
sealed class AIOperationState {
    object Idle : AIOperationState()
    object Loading : AIOperationState()
    data class Success<T>(val data: T) : AIOperationState()
    data class Error(val message: String) : AIOperationState()
}