package com.heartlessveteran.myriad.data.validation

/**
 * Data validation interface for consistent validation across the app
 */
interface Validator<T> {
    fun validate(data: T): ValidationResult
}

/**
 * Result of data validation
 */
sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val errors: List<ValidationError>) : ValidationResult
}

/**
 * Individual validation error
 */
data class ValidationError(
    val field: String,
    val message: String,
    val code: String
)

/**
 * Manga validation implementation
 */
class MangaValidator : Validator<com.heartlessveteran.myriad.domain.entities.Manga> {
    
    override fun validate(data: com.heartlessveteran.myriad.domain.entities.Manga): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Title validation
        if (data.title.isBlank()) {
            errors.add(ValidationError("title", "Title cannot be empty", "TITLE_EMPTY"))
        }
        
        if (data.title.length > 500) {
            errors.add(ValidationError("title", "Title cannot exceed 500 characters", "TITLE_TOO_LONG"))
        }
        
        // Author validation
        if (data.author.isNotBlank() && data.author.length > 200) {
            errors.add(ValidationError("author", "Author name cannot exceed 200 characters", "AUTHOR_TOO_LONG"))
        }
        
        // Rating validation
        if (data.rating < 0f || data.rating > 10f) {
            errors.add(ValidationError("rating", "Rating must be between 0 and 10", "RATING_OUT_OF_RANGE"))
        }
        
        // Chapter validation
        if (data.totalChapters < 0) {
            errors.add(ValidationError("totalChapters", "Total chapters cannot be negative", "TOTAL_CHAPTERS_NEGATIVE"))
        }
        
        if (data.readChapters < 0) {
            errors.add(ValidationError("readChapters", "Read chapters cannot be negative", "READ_CHAPTERS_NEGATIVE"))
        }
        
        if (data.readChapters > data.totalChapters && data.totalChapters > 0) {
            errors.add(ValidationError("readChapters", "Read chapters cannot exceed total chapters", "READ_CHAPTERS_EXCEED_TOTAL"))
        }
        
        // Source validation
        if (data.source.isBlank()) {
            errors.add(ValidationError("source", "Source cannot be empty", "SOURCE_EMPTY"))
        }
        
        // Path validation for local manga
        if (data.isLocal && data.localPath.isNullOrBlank()) {
            errors.add(ValidationError("localPath", "Local path is required for local manga", "LOCAL_PATH_REQUIRED"))
        }
        
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
}

/**
 * Extension functions for validation
 */
fun ValidationResult.isValid(): Boolean = this is ValidationResult.Valid

fun ValidationResult.getErrors(): List<ValidationError> {
    return when (this) {
        is ValidationResult.Valid -> emptyList()
        is ValidationResult.Invalid -> errors
    }
}

fun ValidationResult.getErrorMessages(): List<String> {
    return getErrors().map { it.message }
}

/**
 * Exception thrown when validation fails
 */
class ValidationException(val validationResult: ValidationResult.Invalid) : 
    Exception("Validation failed: ${validationResult.errors.joinToString { it.message }}")