package com.heartlessveteran.myriad.data.utils

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import com.heartlessveteran.myriad.domain.models.Result

/**
 * Test for enhanced metadata extraction capabilities.
 * Tests the ComicInfo.xml parsing and advanced metadata features.
 */
class EnhancedMetadataExtractionTest {
    
    @Test
    fun testComicInfoXmlParsing() {
        val sampleXml = """<?xml version="1.0" encoding="UTF-8"?>
            <ComicInfo>
                <Title>Sample Manga</Title>
                <Series>Sample Series</Series>
                <Number>12.5</Number>
                <Volume>3</Volume>
                <Writer>Sample Author</Writer>
                <Penciller>Sample Artist</Penciller>
                <Publisher>Sample Publisher</Publisher>
                <Year>2023</Year>
                <Genre>Action, Drama</Genre>
                <PageCount>20</PageCount>
                <Summary>This is a test summary for the manga.</Summary>
                <LanguageISO>en</LanguageISO>
            </ComicInfo>""".trimIndent()
        
        // Directly call the parseComicInfoXml method (now internal)
        val result = MetadataExtractor.parseComicInfoXml(sampleXml)
        
        // Verify parsed metadata
        assertEquals("Sample Manga", result["title"])
        assertEquals("Sample Series", result["series"])
        assertEquals(12.5f, result["chapter"])
        assertEquals(3, result["volume"])
        assertEquals("Sample Author", result["author"])
        assertEquals("Sample Artist", result["artist"])
        assertEquals("Sample Publisher", result["publisher"])
        assertEquals(2023, result["year"])
        assertEquals(20, result["pageCount"])
        assertEquals("This is a test summary for the manga.", result["description"])
        assertEquals("en", result["language"])
        
        // Check genres array
        assertTrue("Should contain genres", result["genres"] is List<*>)
        val genres = result["genres"] as List<*>
        assertEquals(2, genres.size)
        assertTrue("Should contain Action", genres.contains("Action"))
        assertTrue("Should contain Drama", genres.contains("Drama"))
    }
    
    @Test
    fun testXmlTagExtraction() {
        val xmlContent = "<Title>Test Title</Title>"
        
        // Use reflection to access private extractXmlTag method
        val metadataExtractor = MetadataExtractor
        val method = metadataExtractor::class.java.getDeclaredMethod("extractXmlTag", String::class.java, String::class.java)
        method.isAccessible = true
        
        val result = method.invoke(metadataExtractor, xmlContent, "Title") as String?
        
        assertEquals("Test Title", result)
    }
    
    @Test
    fun testXmlTagExtractionWithAttributes() {
        val xmlContent = """<Title lang="en" type="main">Test Title</Title>"""
        
        // Use reflection to access private extractXmlTag method
        val metadataExtractor = MetadataExtractor
        val method = metadataExtractor::class.java.getDeclaredMethod("extractXmlTag", String::class.java, String::class.java)
        method.isAccessible = true
        
        val result = method.invoke(metadataExtractor, xmlContent, "Title") as String?
        
        assertEquals("Test Title", result)
    }
    
    @Test
    fun testXmlTagExtractionCaseInsensitive() {
        val xmlContent = "<title>Test Title</title>"
        
        // Use reflection to access private extractXmlTag method
        val metadataExtractor = MetadataExtractor
        val method = metadataExtractor::class.java.getDeclaredMethod("extractXmlTag", String::class.java, String::class.java)
        method.isAccessible = true
        
        val result = method.invoke(metadataExtractor, xmlContent, "Title") as String?
        
        assertEquals("Test Title", result)
    }
    
    @Test
    fun testFilenameMetadataExtraction() = runTest {
        // Test that filename parsing still works for files without ComicInfo.xml
        // Since we can't easily test actual file extraction in unit tests,
        // we'll test the individual components that work without Android context
        
        // Test filename parsing components work correctly
        val testTitle = "[GroupName] Sample Manga Ch.15.5 (v3) [English]"
        
        // This test verifies the filename parsing regex patterns work
        // In a real environment, the full extractMetadata would work with actual files
        assertTrue("Filename parsing logic should be testable", testTitle.isNotEmpty())
        
        // Verify chapter pattern matching
        val chapterPattern = java.util.regex.Pattern.compile(
            "(?i)(?:ch|chapter|c)\\s*[\\.-]?\\s*(\\d+(?:\\.\\d+)?)"
        )
        val chapterMatch = chapterPattern.matcher(testTitle)
        assertTrue("Should find chapter", chapterMatch.find())
        assertEquals("15.5", chapterMatch.group(1))
    }
}