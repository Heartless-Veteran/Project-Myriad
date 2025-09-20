package com.heartlessveteran.myriad.ui.viewmodel

import com.heartlessveteran.myriad.domain.entities.Manga
import com.heartlessveteran.myriad.domain.entities.MangaStatus
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.repository.SourceRepository
import com.heartlessveteran.myriad.domain.usecase.GetLatestMangaUseCase
import com.heartlessveteran.myriad.domain.usecase.SearchMangaUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit test for BrowseViewModel to verify basic functionality
 */
class BrowseViewModelTest {
    @Test
    fun `initial state should show loading`() {
        // Given
        val mockGetLatestUseCase =
            GetLatestMangaUseCase(
                object : SourceRepository {
                    override fun getLatestManga(page: Int): Flow<Result<List<Manga>>> =
                        flowOf(Result.Success(emptyList()))

                    override fun searchManga(
                        query: String,
                        page: Int,
                    ): Flow<Result<List<Manga>>> = flowOf(Result.Success(emptyList()))
                },
            )
        val mockSearchUseCase =
            SearchMangaUseCase(
                object : SourceRepository {
                    override fun getLatestManga(page: Int): Flow<Result<List<Manga>>> =
                        flowOf(Result.Success(emptyList()))

                    override fun searchManga(
                        query: String,
                        page: Int,
                    ): Flow<Result<List<Manga>>> = flowOf(Result.Success(emptyList()))
                },
            )

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
    fun `successful manga loading should update state correctly`() =
        runTest {
            // Given
            val testManga =
                listOf(
                    Manga(
                        title = "Test Manga",
                        artist = "Test Artist",
                        author = "Test Author",
                        description = "Test Description",
                        genres = listOf("Action", "Adventure"),
                        status = MangaStatus.ONGOING,
                        coverImageUrl = "test-thumbnail-url",
                    ),
                )

            val mockGetLatestUseCase =
                GetLatestMangaUseCase(
                    object : SourceRepository {
                        override fun getLatestManga(page: Int): Flow<Result<List<Manga>>> =
                            flowOf(Result.Success(testManga))

                        override fun searchManga(
                            query: String,
                            page: Int,
                        ): Flow<Result<List<Manga>>> = flowOf(Result.Success(emptyList()))
                    },
                )
            val mockSearchUseCase =
                SearchMangaUseCase(
                    object : SourceRepository {
                        override fun getLatestManga(page: Int): Flow<Result<List<Manga>>> =
                            flowOf(Result.Success(testManga))

                        override fun searchManga(
                            query: String,
                            page: Int,
                        ): Flow<Result<List<Manga>>> = flowOf(Result.Success(emptyList()))
                    },
                )

            // When
            val viewModel = BrowseViewModel(mockGetLatestUseCase, mockSearchUseCase)

            // Advance coroutine execution until idle
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse("Loading should be false after success", state.isLoading)
            assertEquals("Should have test manga", testManga, state.manga)
            assertNull("Error should be null on success", state.error)
        }
}
