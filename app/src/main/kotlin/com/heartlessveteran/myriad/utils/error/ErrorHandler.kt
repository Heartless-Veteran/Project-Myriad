package com.heartlessveteran.myriad.utils.error

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Global Error Handling Framework - Phase 1 Implementation
 * Provides centralized error management with user-friendly messages
 */
sealed class AppError(
    val message: String,
    val userMessage: String,
    val throwable: Throwable? = null
) {
    class NetworkError(message: String, throwable: Throwable? = null) : 
        AppError(message, "Network error. Please check your connection.", throwable)
    
    class FileError(message: String, throwable: Throwable? = null) : 
        AppError(message, "File operation failed. Please try again.", throwable)
    
    class ImportError(message: String, throwable: Throwable? = null) : 
        AppError(message, "Failed to import file. Check the file format.", throwable)
    
    class ReaderError(message: String, throwable: Throwable? = null) : 
        AppError(message, "Reader error. Unable to display content.", throwable)
    
    class GeneralError(message: String, throwable: Throwable? = null) : 
        AppError(message, "Something went wrong. Please try again.", throwable)
}

/**
 * Central error handler for the application
 */
interface ErrorHandler {
    val errors: SharedFlow<AppError>
    fun handleError(error: AppError)
    fun handleError(throwable: Throwable, context: String = "")
}

class ErrorHandlerImpl : ErrorHandler {
    
    private val _errors = MutableSharedFlow<AppError>()
    override val errors: SharedFlow<AppError> = _errors.asSharedFlow()
    
    override fun handleError(error: AppError) {
        _errors.tryEmit(error)
    }
    
    override fun handleError(throwable: Throwable, context: String) {
        val appError = when {
            throwable.message?.contains("network", ignoreCase = true) == true ||
            throwable.message?.contains("connection", ignoreCase = true) == true -> {
                AppError.NetworkError("Network error in $context", throwable)
            }
            
            throwable.message?.contains("file", ignoreCase = true) == true ||
            throwable is java.io.IOException -> {
                AppError.FileError("File error in $context", throwable)
            }
            
            throwable.message?.contains("import", ignoreCase = true) == true ||
            throwable.message?.contains("unsupported", ignoreCase = true) == true -> {
                AppError.ImportError("Import error in $context", throwable)
            }
            
            context.contains("reader", ignoreCase = true) -> {
                AppError.ReaderError("Reader error in $context", throwable)
            }
            
            else -> {
                AppError.GeneralError("Error in $context: ${throwable.message}", throwable)
            }
        }
        
        handleError(appError)
    }
}

/**
 * Extension functions for easier error handling with Result
 */
suspend fun <T> Result<T>.handleError(
    errorHandler: ErrorHandler,
    context: String = ""
): Result<T> {
    onFailure { throwable ->
        errorHandler.handleError(throwable, context)
    }
    return this
}

/**
 * Safe execution wrapper
 */
suspend inline fun <T> safeCall(
    errorHandler: ErrorHandler,
    context: String = "",
    crossinline block: suspend () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        errorHandler.handleError(e, context)
        Result.failure(e)
    }
}