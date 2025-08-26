package com.heartlessveteran.myriad.ui.common

/**
 * Generic UI state wrapper for consistent state management across the app
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    
    data class Success<T>(val data: T) : UiState<T>
    
    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "Unknown error occurred"
    ) : UiState<Nothing>
}

/**
 * Extension functions for UiState handling
 */
inline fun <T> UiState<T>.onSuccess(action: (value: T) -> Unit): UiState<T> {
    if (this is UiState.Success) action(data)
    return this
}

inline fun <T> UiState<T>.onError(action: (exception: Throwable) -> Unit): UiState<T> {
    if (this is UiState.Error) action(exception)
    return this
}

inline fun <T> UiState<T>.onLoading(action: () -> Unit): UiState<T> {
    if (this is UiState.Loading) action()
    return this
}

/**
 * Transform UiState data while preserving state type
 */
inline fun <T, R> UiState<T>.map(transform: (value: T) -> R): UiState<R> {
    return when (this) {
        is UiState.Loading -> UiState.Loading
        is UiState.Success -> UiState.Success(transform(data))
        is UiState.Error -> this
    }
}

/**
 * Combine two UiStates
 */
inline fun <T1, T2, R> combineUiStates(
    state1: UiState<T1>,
    state2: UiState<T2>,
    transform: (T1, T2) -> R
): UiState<R> {
    return when {
        state1 is UiState.Loading || state2 is UiState.Loading -> UiState.Loading
        state1 is UiState.Error -> state1
        state2 is UiState.Error -> state2
        state1 is UiState.Success && state2 is UiState.Success -> {
            UiState.Success(transform(state1.data, state2.data))
        }
        else -> UiState.Error(IllegalStateException("Invalid state combination"))
    }
}