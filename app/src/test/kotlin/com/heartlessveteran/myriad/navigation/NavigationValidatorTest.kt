package com.heartlessveteran.myriad.navigation

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for NavigationValidator
 */
class NavigationValidatorTest {
    @Test
    fun `validateMangaId should return true for valid manga ID`() {
        assertTrue(NavigationValidator.validateMangaId("valid-manga-id"))
        assertTrue(NavigationValidator.validateMangaId("123"))
        assertTrue(NavigationValidator.validateMangaId("manga-with-dashes"))
    }

    @Test
    fun `validateMangaId should return false for null or blank manga ID`() {
        assertFalse(NavigationValidator.validateMangaId(null))
        assertFalse(NavigationValidator.validateMangaId(""))
        assertFalse(NavigationValidator.validateMangaId("   "))
    }

    @Test
    fun `validateMangaId should return false for manga ID exceeding 255 characters`() {
        val longId = "a".repeat(256)
        assertFalse(NavigationValidator.validateMangaId(longId))
    }

    @Test
    fun `validateMangaId should return true for manga ID of exactly 255 characters`() {
        val maxLengthId = "a".repeat(255)
        assertTrue(NavigationValidator.validateMangaId(maxLengthId))
    }

    @Test
    fun `validateAnimeId should return true for valid anime ID`() {
        assertTrue(NavigationValidator.validateAnimeId("valid-anime-id"))
        assertTrue(NavigationValidator.validateAnimeId("456"))
        assertTrue(NavigationValidator.validateAnimeId("anime-with-dashes"))
    }

    @Test
    fun `validateAnimeId should return false for null or blank anime ID`() {
        assertFalse(NavigationValidator.validateAnimeId(null))
        assertFalse(NavigationValidator.validateAnimeId(""))
        assertFalse(NavigationValidator.validateAnimeId("   "))
    }

    @Test
    fun `validateAnimeId should return false for anime ID exceeding 255 characters`() {
        val longId = "a".repeat(256)
        assertFalse(NavigationValidator.validateAnimeId(longId))
    }

    @Test
    fun `validatePage should return true for valid page numbers`() {
        assertTrue(NavigationValidator.validatePage("0"))
        assertTrue(NavigationValidator.validatePage("1"))
        assertTrue(NavigationValidator.validatePage("100"))
    }

    @Test
    fun `validatePage should return false for negative page numbers`() {
        assertFalse(NavigationValidator.validatePage("-1"))
        assertFalse(NavigationValidator.validatePage("-100"))
    }

    @Test
    fun `validatePage should return false for invalid page format`() {
        assertFalse(NavigationValidator.validatePage("not-a-number"))
        assertFalse(NavigationValidator.validatePage("1.5"))
        assertFalse(NavigationValidator.validatePage(""))
    }

    @Test
    fun `validatePage should return false for null`() {
        assertFalse(NavigationValidator.validatePage(null))
    }

    @Test
    fun `validateTimestamp should return true for valid timestamps`() {
        assertTrue(NavigationValidator.validateTimestamp("0"))
        assertTrue(NavigationValidator.validateTimestamp("1000"))
        assertTrue(NavigationValidator.validateTimestamp("1647875400000"))
    }

    @Test
    fun `validateTimestamp should return false for negative timestamps`() {
        assertFalse(NavigationValidator.validateTimestamp("-1"))
        assertFalse(NavigationValidator.validateTimestamp("-1000"))
    }

    @Test
    fun `validateTimestamp should return false for invalid timestamp format`() {
        assertFalse(NavigationValidator.validateTimestamp("not-a-number"))
        assertFalse(NavigationValidator.validateTimestamp("1.5"))
        assertFalse(NavigationValidator.validateTimestamp(""))
    }

    @Test
    fun `validateTimestamp should return false for null`() {
        assertFalse(NavigationValidator.validateTimestamp(null))
    }

    @Test
    fun `validateSearchQuery should return true for valid queries`() {
        assertTrue(NavigationValidator.validateSearchQuery("One Piece"))
        assertTrue(NavigationValidator.validateSearchQuery(""))
        assertTrue(NavigationValidator.validateSearchQuery(null))
        assertTrue(NavigationValidator.validateSearchQuery("a".repeat(500)))
    }

    @Test
    fun `validateSearchQuery should return false for query exceeding 500 characters`() {
        val longQuery = "a".repeat(501)
        assertFalse(NavigationValidator.validateSearchQuery(longQuery))
    }

    @Test
    fun `validateContentType should return true for valid content types`() {
        assertTrue(NavigationValidator.validateContentType("ALL"))
        assertTrue(NavigationValidator.validateContentType("MANGA"))
        assertTrue(NavigationValidator.validateContentType("ANIME"))
        assertTrue(NavigationValidator.validateContentType("all"))
        assertTrue(NavigationValidator.validateContentType("manga"))
        assertTrue(NavigationValidator.validateContentType("anime"))
        assertTrue(NavigationValidator.validateContentType(null))
    }

    @Test
    fun `validateContentType should return false for invalid content types`() {
        assertFalse(NavigationValidator.validateContentType("INVALID"))
        assertFalse(NavigationValidator.validateContentType("BOOK"))
        assertFalse(NavigationValidator.validateContentType(""))
    }

    @Test
    fun `validateSettingsSection should return true for valid settings sections`() {
        assertTrue(NavigationValidator.validateSettingsSection("GENERAL"))
        assertTrue(NavigationValidator.validateSettingsSection("READING"))
        assertTrue(NavigationValidator.validateSettingsSection("WATCHING"))
        assertTrue(NavigationValidator.validateSettingsSection("SOURCES"))
        assertTrue(NavigationValidator.validateSettingsSection("STORAGE"))
        assertTrue(NavigationValidator.validateSettingsSection("AI"))
        assertTrue(NavigationValidator.validateSettingsSection("ABOUT"))
        assertTrue(NavigationValidator.validateSettingsSection("general"))
        assertTrue(NavigationValidator.validateSettingsSection(null))
    }

    @Test
    fun `validateSettingsSection should return false for invalid settings sections`() {
        assertFalse(NavigationValidator.validateSettingsSection("INVALID"))
        assertFalse(NavigationValidator.validateSettingsSection("UNKNOWN"))
        assertFalse(NavigationValidator.validateSettingsSection(""))
    }
}

/**
 * Unit tests for Destination route creation
 */
class DestinationTest {
    @Test
    fun `Reading destination should create correct route`() {
        val route = Destination.Reading.createRoute("manga-123", "chapter-456", 5)
        assertTrue(route.contains("manga-123"))
        assertTrue(route.contains("chapter-456"))
        assertTrue(route.contains("page=5"))
    }

    @Test
    fun `Reading destination should create route without chapter`() {
        val route = Destination.Reading.createRoute("manga-123", null, 0)
        assertTrue(route.contains("manga-123"))
        assertFalse(route.contains("chapter"))
        assertTrue(route.contains("page=0"))
    }

    @Test
    fun `Watching destination should create correct route`() {
        val route = Destination.Watching.createRoute("anime-789", "episode-101", 3600000L)
        assertTrue(route.contains("anime-789"))
        assertTrue(route.contains("episode-101"))
        assertTrue(route.contains("timestamp=3600000"))
    }

    @Test
    fun `Watching destination should create route without episode`() {
        val route = Destination.Watching.createRoute("anime-789", null, 0L)
        assertTrue(route.contains("anime-789"))
        assertFalse(route.contains("episode"))
        assertTrue(route.contains("timestamp=0"))
    }

    @Test
    fun `MangaDetail destination should create correct route`() {
        val route = Destination.MangaDetail.createRoute("manga-123", "source-456")
        assertTrue(route.contains("manga-123"))
        assertTrue(route.contains("sourceId=source-456"))
    }

    @Test
    fun `MangaDetail destination should create route without source`() {
        val route = Destination.MangaDetail.createRoute("manga-123", null)
        assertTrue(route.contains("manga-123"))
        assertFalse(route.contains("sourceId"))
    }

    @Test
    fun `Search destination should create correct route`() {
        val route = Destination.Search.createRoute("One Piece", ContentType.MANGA, "mangadex")
        assertTrue(route.contains("One%20Piece"))
        assertTrue(route.contains("type=MANGA"))
        assertTrue(route.contains("source=mangadex"))
    }

    @Test
    fun `Search destination should handle empty query`() {
        val route = Destination.Search.createRoute("", ContentType.ALL, null)
        assertTrue(route.contains("query="))
        assertTrue(route.contains("type=ALL"))
        assertFalse(route.contains("source="))
    }

    @Test
    fun `Settings destination should create correct route`() {
        val route = Destination.Settings.createRoute(SettingsSection.READING)
        assertTrue(route.contains("settings/reading"))
    }

    @Test
    fun `Settings destination should use general as default`() {
        val route = Destination.Settings.createRoute()
        assertTrue(route.contains("settings/general"))
    }
}
