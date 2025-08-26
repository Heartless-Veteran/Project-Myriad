package com.heartlessveteran.myriad.ui.viewmodel

import com.heartlessveteran.myriad.domain.model.Manga
import com.heartlessveteran.myriad.domain.usecase.GetLatestMangaUseCase
import com.heartlessveteran.myriad.domain.usecase.SearchMangaUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit test for BrowseViewModel to verify basic functionality
 */
class BrowseViewModelTest {

    @Test
    fun `initial state should show loading`() {
        // Given
        val mockGetLatestUseCase = GetLatestMangaUseCase(object : com.heartlessveteran.myriad.domain.repository.SourceRepository {
            override fun getLatestManga(page: Int) = flowOf(Result.success(emptyList<Manga>()))
            override fun searchManga(query: String, page: Int) = flowOf(Result.success(emptyList<Manga>()))
        })
        val mockSearchUseCase = SearchMangaUseCase(object : com.heartlessveteran.myriad.domain.repository.SourceRepository {
            override fun getLatestManga(page: Int) = flowOf(Result.success(emptyList<Manga>()))
            override fun searchManga(query: String, page: Int) = flowOf(Result.success(emptyList<Manga>()))
        })

        // When
        val viewModel = BrowseViewModel(mockGetLatestUseCase, mockSearchUseCase)

        // Then
        val initialState = viewModel.uiState.value
        assertTrue("Initial state should show loading", initialState.isLoading)
        assertEquals("Initial page should be 1", 1, initialState.page)
        assertEquals("Initial search query should be empty", "", initialState.searchQuery)
        assertFalse("Initial state should not be searching", initialState.isSearching)
    }
    
    @Test
    fun `successful manga loading should update state correctly`() = runTest {
        // Given
        val testManga = listOf(
            Manga(
                url = "test-url",
                title = "Test Manga",
                artist = "Test Artist",
                author = "Test Author", 
                description = "Test Description",
                genre = listOf("Action", "Adventure"),
                status = "Ongoing",
                thumbnailUrl = "test-thumbnail-url"
            )
        )
        
        val mockGetLatestUseCase = GetLatestMangaUseCase(object : com.heartlessveteran.myriad.domain.repository.SourceRepository {
            override fun getLatestManga(page: Int) = flowOf(Result.success(testManga))
            override fun searchManga(query: String, page: Int) = flowOf(Result.success(emptyList<Manga>()))
        })
        val mockSearchUseCase = SearchMangaUseCase(object : com.heartlessveteran.myriad.domain.repository.SourceRepository {
            override fun getLatestManga(page: Int) = flowOf(Result.success(testManga))
            override fun searchManga(query: String, page: Int) = flowOf(Result.success(emptyList<Manga>()))
        })

        // When
        val viewModel = BrowseViewModel(mockGetLatestUseCase, mockSearchUseCase)
        
        // Give some time for the flow to emit
        kotlinx.coroutines.delay(100)

        // Then
        val state = viewModel.uiState.value
        assertFalse("Loading should be false after success", state.isLoading)
        assertEquals("Should have test manga", testManga, state.manga)
        assertNull("Error should be null on success", state.error)
    }
}