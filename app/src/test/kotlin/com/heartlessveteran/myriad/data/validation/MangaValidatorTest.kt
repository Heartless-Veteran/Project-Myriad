package com.heartlessveteran.myriad.data.validation

import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.entities.MangaStatus
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Unit tests for MangaValidator
 */
class MangaValidatorTest {
    
    private lateinit var validator: MangaValidator
    
    @Before
    fun setUp() {
        validator = MangaValidator()
    }
    
    @Test
    fun `validate should return Valid for valid manga`() {
        val manga = createValidManga()
        
        val result = validator.validate(manga)
        
        assertTrue(result.isValid())
    }
    
    @Test
    fun `validate should return Invalid when title is empty`() {
        val manga = createValidManga().copy(title = "")
        
        val result = validator.validate(manga)
        
        assertFalse(result.isValid())
        val errors = result.getErrors()
        assertTrue(errors.any { it.field == "title" && it.code == "TITLE_EMPTY" })
    }
    
    @Test
    fun `validate should return Invalid when title is blank`() {
        val manga = createValidManga().copy(title = "   ")
        
        val result = validator.validate(manga)
        
        assertFalse(result.isValid())
        val errors = result.getErrors()
        assertTrue(errors.any { it.field == "title" && it.code == "TITLE_EMPTY" })
    }
    
    @Test
    fun `validate should return Invalid when title exceeds 500 characters`() {
        val longTitle = "a".repeat(501)
        val manga = createValidManga().copy(title = longTitle)
        
        val result = validator.validate(manga)
        
        assertFalse(result.isValid())
        val errors = result.getErrors()
        assertTrue(errors.any { it.field == "title" && it.code == "TITLE_TOO_LONG" })
    }
    
    @Test
    fun `validate should return Invalid when author exceeds 200 characters`() {
        val longAuthor = "a".repeat(201)
        val manga = createValidManga().copy(author = longAuthor)
        
        val result = validator.validate(manga)
        
        assertFalse(result.isValid())
        val errors = result.getErrors()
        assertTrue(errors.any { it.field == "author" && it.code == "AUTHOR_TOO_LONG" })
    }
    
    @Test
    fun `validate should accept empty author`() {
        val manga = createValidManga().copy(author = "")
        
        val result = validator.validate(manga)
        
        assertTrue(result.isValid())
    }
    
    @Test
    fun `validate should return Invalid when rating is negative`() {
        val manga = createValidManga().copy(rating = -1f)
        
        val result = validator.validate(manga)
        
        assertFalse(result.isValid())
        val errors = result.getErrors()
        assertTrue(errors.any { it.field == "rating" && it.code == "RATING_OUT_OF_RANGE" })
    }
    
    @Test
    fun `validate should return Invalid when rating exceeds 10`() {
        val manga = createValidManga().copy(rating = 11f)
        
        val result = validator.validate(manga)
        
        assertFalse(result.isValid())
        val errors = result.getErrors()
        assertTrue(errors.any { it.field == "rating" && it.code == "RATING_OUT_OF_RANGE" })
    }
    
    @Test
    fun `validate should accept rating of 0`() {
        val manga = createValidManga().copy(rating = 0f)
        
        val result = validator.validate(manga)
        
        assertTrue(result.isValid())
    }
    
    @Test
    fun `validate should accept rating of 10`() {
        val manga = createValidManga().copy(rating = 10f)
        
        val result = validator.validate(manga)
        
        assertTrue(result.isValid())
    }
    
    @Test
    fun `validate should return Invalid when totalChapters is negative`() {
        val manga = createValidManga().copy(totalChapters = -1)
        
        val result = validator.validate(manga)
        
        assertFalse(result.isValid())
        val errors = result.getErrors()
        assertTrue(errors.any { it.field == "totalChapters" && it.code == "TOTAL_CHAPTERS_NEGATIVE" })
    }
    
    @Test
    fun `validate should return Invalid when readChapters is negative`() {
        val manga = createValidManga().copy(readChapters = -1)
        
        val result = validator.validate(manga)
        
        assertFalse(result.isValid())
        val errors = result.getErrors()
        assertTrue(errors.any { it.field == "readChapters" && it.code == "READ_CHAPTERS_NEGATIVE" })
    }
    
    @Test
    fun `validate should return Invalid when readChapters exceeds totalChapters`() {
        val manga = createValidManga().copy(totalChapters = 5, readChapters = 6)
        
        val result = validator.validate(manga)
        
        assertFalse(result.isValid())
        val errors = result.getErrors()
        assertTrue(errors.any { it.field == "readChapters" && it.code == "READ_CHAPTERS_EXCEED_TOTAL" })
    }
    
    @Test
    fun `validate should accept readChapters equal to totalChapters`() {
        val manga = createValidManga().copy(totalChapters = 5, readChapters = 5)
        
        val result = validator.validate(manga)
        
        assertTrue(result.isValid())
    }
    
    @Test
    fun `validate should allow readChapters exceeding totalChapters when totalChapters is 0`() {
        val manga = createValidManga().copy(totalChapters = 0, readChapters = 5)
        
        val result = validator.validate(manga)
        
        assertTrue(result.isValid())
    }
    
    @Test
    fun `validate should return Invalid when source is empty`() {
        val manga = createValidManga().copy(source = "")
        
        val result = validator.validate(manga)
        
        assertFalse(result.isValid())
        val errors = result.getErrors()
        assertTrue(errors.any { it.field == "source" && it.code == "SOURCE_EMPTY" })
    }
    
    @Test
    fun `validate should return Invalid when local manga has no localPath`() {
        val manga = createValidManga().copy(isLocal = true, localPath = null)
        
        val result = validator.validate(manga)
        
        assertFalse(result.isValid())
        val errors = result.getErrors()
        assertTrue(errors.any { it.field == "localPath" && it.code == "LOCAL_PATH_REQUIRED" })
    }
    
    @Test
    fun `validate should return Invalid when local manga has empty localPath`() {
        val manga = createValidManga().copy(isLocal = true, localPath = "")
        
        val result = validator.validate(manga)
        
        assertFalse(result.isValid())
        val errors = result.getErrors()
        assertTrue(errors.any { it.field == "localPath" && it.code == "LOCAL_PATH_REQUIRED" })
    }
    
    @Test
    fun `validate should accept non-local manga without localPath`() {
        val manga = createValidManga().copy(isLocal = false, localPath = null)
        
        val result = validator.validate(manga)
        
        assertTrue(result.isValid())
    }
    
    @Test
    fun `validate should return multiple errors for multiple violations`() {
        val manga = createValidManga().copy(
            title = "",
            rating = -1f,
            totalChapters = -1,
            source = ""
        )
        
        val result = validator.validate(manga)
        
        assertFalse(result.isValid())
        val errors = result.getErrors()
        assertTrue(errors.size >= 4)
        assertTrue(errors.any { it.code == "TITLE_EMPTY" })
        assertTrue(errors.any { it.code == "RATING_OUT_OF_RANGE" })
        assertTrue(errors.any { it.code == "TOTAL_CHAPTERS_NEGATIVE" })
        assertTrue(errors.any { it.code == "SOURCE_EMPTY" })
    }
    
    @Test
    fun `ValidationResult getErrorMessages should return list of error messages`() {
        val manga = createValidManga().copy(title = "", rating = -1f)
        
        val result = validator.validate(manga)
        
        val messages = result.getErrorMessages()
        assertTrue(messages.contains("Title cannot be empty"))
        assertTrue(messages.contains("Rating must be between 0 and 10"))
    }
    
    @Test
    fun `ValidationException should contain validation result`() {
        val errors = listOf(
            ValidationError("title", "Title cannot be empty", "TITLE_EMPTY")
        )
        val validationResult = ValidationResult.Invalid(errors)
        
        val exception = ValidationException(validationResult)
        
        assertEquals(validationResult, exception.validationResult)
        assertTrue(exception.message!!.contains("Title cannot be empty"))
    }
    
    private fun createValidManga(): Manga {
        return Manga(
            id = "test-id",
            title = "Test Manga",
            author = "Test Author",
            artist = "Test Artist",
            description = "Test Description",
            status = MangaStatus.ONGOING,
            rating = 8.5f,
            totalChapters = 10,
            readChapters = 5,
            source = "test-source",
            isLocal = false,
            localPath = null,
            dateAdded = Date(),
            lastUpdated = Date()
        )
    }
}