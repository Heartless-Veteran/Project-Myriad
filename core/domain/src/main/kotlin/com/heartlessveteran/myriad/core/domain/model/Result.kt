package com.heartlessveteran.myriad.core.domain.model

/**
 * A generic wrapper for handling success, error, and loading states in the application.
 * Follows the architecture guidelines for proper error handling.
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation with data
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents an error state with exception and optional message
     */
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()

    /**
     * Represents a loading state
     */
    data object Loading : Result<Nothing>()

    /**
     * Returns true if this is a Success result
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Returns true if this is an Error result
     */
    fun isError(): Boolean = this is Error

    /**
     * Returns true if this is a Loading result
     */
    fun isLoading(): Boolean = this is Loading

    /**
     * Returns the data if Success, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Returns the data if Success, throws exception if Error
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Cannot get data from loading state")
    }
}

/**
 * Extension function to map Result data
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
    is Result.Loading -> this
}

/**
 * Extension function to handle Result states
 */
inline fun <T> Result<T>.fold(
    onSuccess: (T) -> Unit = {},
    onError: (Throwable, String?) -> Unit = { _, _ -> },
    onLoading: () -> Unit = {}
) {
    when (this) {
        is Result.Success -> onSuccess(data)
        is Result.Error -> onError(exception, message)
        is Result.Loading -> onLoading()
    }
}