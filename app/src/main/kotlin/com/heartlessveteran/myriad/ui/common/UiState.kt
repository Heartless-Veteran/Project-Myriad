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
 * Executes [action] with the contained value when this state is [UiState.Success], then returns the original state.
 *
 * The provided [action] is invoked only for `Success` states with the success value as its argument.
 *
 * @param action Callback invoked with the success value when this state is `Success`.
 * @return The original `UiState<T>` instance (useful for fluent/chained calls).
 */
inline fun <T> UiState<T>.onSuccess(action: (value: T) -> Unit): UiState<T> {
    if (this is UiState.Success) action(data)
    return this
}

/**
 * Executes [action] with the error's exception if this state is [UiState.Error], then returns this state.
 *
 * @param action Callback invoked with the stored [Throwable] when the receiver is an [UiState.Error].
 * @return The original [UiState] instance to allow fluent chaining.
 */
inline fun <T> UiState<T>.onError(action: (exception: Throwable) -> Unit): UiState<T> {
    if (this is UiState.Error) action(exception)
    return this
}

/**
 * Executes [action] if this state is `UiState.Loading`, then returns the original state.
 *
 * Useful for performing a side-effect when the UI is loading while preserving fluent chaining.
 *
 * @param action Lambda invoked when the receiver is `Loading`.
 * @return The original `UiState<T>` instance (unchanged) to allow chaining.
 */
inline fun <T> UiState<T>.onLoading(action: () -> Unit): UiState<T> {
    if (this is UiState.Loading) action()
    return this
}

/**
 * Maps the successful value inside this UiState to another type, preserving the surrounding state.
 *
 * - If this is [UiState.Success], applies [transform] to the contained value and returns `UiState.Success(result)`.
 * - If this is [UiState.Loading], returns [UiState.Loading].
 * - If this is [UiState.Error], returns the same [UiState.Error] unchanged.
 *
 * @param transform Function to convert the contained success value from `T` to `R`.
 * @return A `UiState<R>` reflecting the mapped success value or the original non-success state.
 */
inline fun <T, R> UiState<T>.map(transform: (value: T) -> R): UiState<R> {
    return when (this) {
        is UiState.Loading -> UiState.Loading
        is UiState.Success -> UiState.Success(transform(data))
        is UiState.Error -> this
    }
}

/**
 * Combines two UiState values into a single UiState by applying `transform` to both successes.
 *
 * The precedence rules are:
 * - If either state is Loading, returns UiState.Loading.
 * - If `state1` is UiState.Error, returns `state1`.
 * - If `state2` is UiState.Error, returns `state2`.
 * - If both are UiState.Success, returns UiState.Success(transform(state1.data, state2.data)).
 * - Otherwise returns UiState.Error with an IllegalStateException describing an invalid combination.
 *
 * @param state1 The first UiState to combine.
 * @param state2 The second UiState to combine.
 * @param transform Function that combines the successful values from both states into a result of type R.
 * @return A UiState<R> representing the combined result according to the rules above.
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