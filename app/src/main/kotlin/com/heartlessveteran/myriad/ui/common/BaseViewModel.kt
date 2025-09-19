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
    initialState: UiStateType,
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
    protected val errorHandler =
        CoroutineExceptionHandler { _, exception ->
            handleError(exception)
        }

    /**
     * Launches a coroutine in the ViewModel scope to run a suspending block with standardized
     * loading and error handling.
     *
     * The coroutine is launched in `viewModelScope` with the view model's `errorHandler`.
     * If `showLoading` is true, the ViewModel's `isLoading` state is set to true before the
     * block runs and reset to false after completion. Any exception thrown by the block is
     * forwarded to [handleError].
     *
     * @param showLoading When true (default), toggles the ViewModel's `isLoading` state
     *   around the execution of `block`.
     * @param block The suspending operation to execute; exceptions from this block are
     *   handled via [handleError].
     */
    protected fun launchWithErrorHandling(
        showLoading: Boolean = true,
        block: suspend () -> Unit,
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
     * Map an exception to a user-facing error message and publish it to the ViewModel's error state.
     *
     * Converts common network-related exceptions into concise, user-readable messages (e.g., unknown host ->
     * "No internet connection", socket timeout -> "Request timed out", connect exception -> "Failed to connect to server")
     * and assigns the result to the internal error message StateFlow.
     *
     * Override to provide custom mapping or additional handling; the method itself does not throw.
     *
     * @param exception The throwable to map to a user-facing message.
     */
    protected open fun handleError(exception: Throwable) {
        _errorMessage.value =
            when (exception) {
                is java.net.UnknownHostException -> "No internet connection"
                is java.net.SocketTimeoutException -> "Request timed out"
                is java.net.ConnectException -> "Failed to connect to server"
                else -> exception.message ?: "An unknown error occurred"
            }
    }

    /**
     * Clears the current error message.
     *
     * Resets the internal error state to null so observers (e.g., UI) stop showing an active error.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Applies a transformation to the current UI state and updates the internal StateFlow.
     *
     * The provided [update] lambda receives the current state and must return the new state; the result replaces the existing state.
     *
     * @param update Function that maps the current `UiStateType` to a new `UiStateType`.
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
