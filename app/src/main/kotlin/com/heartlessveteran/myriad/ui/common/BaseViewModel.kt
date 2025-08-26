package com.heartlessveteran.myriad.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel with common state management patterns
 */
abstract class BaseViewModel<UiStateType>(
    initialState: UiStateType
) : ViewModel() {
    
    protected val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<UiStateType> = _uiState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    /**
     * Global error handler for coroutines
     */
    protected val errorHandler = CoroutineExceptionHandler { _, exception ->
        handleError(exception)
    }
    
    /**
     * Execute a suspending operation with loading state and error handling
     */
    protected fun launchWithErrorHandling(
        showLoading: Boolean = true,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(errorHandler) {
            if (showLoading) _isLoading.value = true
            try {
                block()
            } catch (e: Exception) {
                handleError(e)
            } finally {
                if (showLoading) _isLoading.value = false
            }
        }
    }
    
    /**
     * Handle errors consistently
     */
    protected open fun handleError(exception: Throwable) {
        _errorMessage.value = when (exception) {
            is java.net.UnknownHostException -> "No internet connection"
            is java.net.SocketTimeoutException -> "Request timed out"
            is java.net.ConnectException -> "Failed to connect to server"
            else -> exception.message ?: "An unknown error occurred"
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Update UI state in a thread-safe manner
     */
    protected fun updateUiState(update: (UiStateType) -> UiStateType) {
        _uiState.value = update(_uiState.value)
    }
    
    /**
     * Get current UI state value
     */
    protected val currentUiState: UiStateType
        get() = _uiState.value
}