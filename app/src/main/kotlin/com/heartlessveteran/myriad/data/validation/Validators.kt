package com.heartlessveteran.myriad.data.validation

/**
 * Data validation interface for consistent validation across the app
 */
interface Validator<T> {
    /**
     * Validate the given data object and return the outcome.
     *
     * @param data The object to validate.
     * @return `ValidationResult.Valid` if the object passed all checks; otherwise `ValidationResult.Invalid` containing the list of `ValidationError`s found.
     */
    fun validate(data: T): ValidationResult
}

/**
 * Result of data validation
 */
sealed interface ValidationResult {
    data object Valid : ValidationResult

    data class Invalid(
        val errors: List<ValidationError>,
    ) : ValidationResult
}

/**
 * Individual validation error
 */
data class ValidationError(
    val field: String,
    val message: String,
    val code: String,
)

/**
 * Manga validation implementation
 */
class MangaValidator : Validator<com.heartlessveteran.myriad.domain.entities.Manga> {
    /**
     * Validates a Manga entity and returns a ValidationResult summarizing any problems.
     *
     * Performs field-level checks and collects ValidationError entries for each violation:
     * - title: must be non-blank and no longer than 500 characters ("TITLE_EMPTY", "TITLE_TOO_LONG")
     * - author: if present, must be no longer than 200 characters ("AUTHOR_TOO_LONG")
     * - rating: must be in the range 0..10 ("RATING_OUT_OF_RANGE")
     * - totalChapters: must not be negative ("TOTAL_CHAPTERS_NEGATIVE")
     * - readChapters: must not be negative and must not exceed totalChapters when totalChapters > 0
     *   ("READ_CHAPTERS_NEGATIVE", "READ_CHAPTERS_EXCEED_TOTAL")
     * - source: must be non-blank ("SOURCE_EMPTY")
     * - localPath: required (non-null, non-blank) when isLocal is true ("LOCAL_PATH_REQUIRED")
     *
     * @param data The Manga instance to validate.
     * @return ValidationResult.Valid if no validation errors were found; otherwise ValidationResult.Invalid containing the list of ValidationError.
     */
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
            errors.add(
                ValidationError(
                    "readChapters",
                    "Read chapters cannot exceed total chapters",
                    "READ_CHAPTERS_EXCEED_TOTAL",
                ),
            )
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
 * Returns whether this validation result represents a successful validation.
 *
 * @return `true` if this is [ValidationResult.Valid]; `false` if it is [ValidationResult.Invalid].
 */
fun ValidationResult.isValid(): Boolean = this is ValidationResult.Valid

/**
 * Returns the list of validation errors for this result.
 *
 * For a `Valid` result this returns an empty list; for `Invalid` it returns the contained errors.
 *
 * @return A list of ValidationError (empty when the result is `Valid`).
 */
fun ValidationResult.getErrors(): List<ValidationError> =
    when (this) {
        is ValidationResult.Valid -> emptyList()
        is ValidationResult.Invalid -> errors
    }

/**
 * Returns the validation error messages as a list of strings.
 *
 * For a `Valid` result this returns an empty list; for `Invalid` it returns the messages of each contained `ValidationError`.
 *
 * @return List of error message strings (empty if there are no errors).
 */
fun ValidationResult.getErrorMessages(): List<String> = getErrors().map { it.message }

/**
 * Exception thrown when validation fails
 */
class ValidationException(
    val validationResult: ValidationResult.Invalid,
) : Exception("Validation failed: ${validationResult.errors.joinToString { it.message }}")
