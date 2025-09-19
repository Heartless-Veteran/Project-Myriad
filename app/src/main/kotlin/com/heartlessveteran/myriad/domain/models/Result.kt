package com.heartlessveteran.myriad.domain.models

/**
 * A sealed class representing the result of an operation that can succeed or fail.
 * This provides a type-safe way to handle success and error states throughout the app.
 */
sealed class Result<out T> {
    /**
     * Represents a successful result containing data of type T
     */
    data class Success<T>(
        val data: T,
    ) : Result<T>()

    /**
     * Represents an error result with an exception and optional message
     */
    data class Error(
        val exception: Throwable,
        val message: String? = null,
    ) : Result<Nothing>()

    /**
     * Represents a loading state
     */
    object Loading : Result<Nothing>()

    /**
     * Returns true if this result is a success
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Returns true if this result is an error
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Returns true if this result is loading
     */
    val isLoading: Boolean
        get() = this is Loading

    /**
     * Returns the data if this result is a success, null otherwise
     */
    fun getOrNull(): T? = if (this is Success) data else null

    /**
     * Returns the exception if this result is an error, null otherwise
     */
    fun exceptionOrNull(): Throwable? = if (this is Error) exception else null

    /**
     * Transforms the success data using the provided transform function
     */
    inline fun <R> map(transform: (value: T) -> R): Result<R> =
        when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(exception, message)
            is Loading -> Loading
        }

    /**
     * Executes the provided action if this result is a success
     */
    inline fun onSuccess(action: (value: T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Executes the provided action if this result is an error
     */
    inline fun onError(action: (exception: Throwable) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }

    /**
     * Executes the provided action if this result is loading
     */
    inline fun onLoading(action: () -> Unit): Result<T> {
        if (this is Loading) action()
        return this
    }
}
