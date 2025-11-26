package com.example.cinephile.ui.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.domain.repository.MovieRepository
import com.example.cinephile.domain.repository.UserCollectionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class SearchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SearchViewModel
    private lateinit var mockMovieRepository: MovieRepository
    private lateinit var mockuserRepo: UserCollectionsRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockMovieRepository = mock()
        mockuserRepo = mock()
        viewModel = SearchViewModel(mockMovieRepository, mockuserRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchMovies with valid query success should update state to Success`() = runTest {
        // Arrange
        val query = "Inception"
        val type = SearchType.TITLE
        val fakeMovies = listOf(
            Movie(1, "Inception", "url1", "url1", "overview1", "2010")
        )
        whenever(mockMovieRepository.searchMovies(query, type)).thenReturn(Result.success(fakeMovies))

        // Act
        viewModel.searchMovies(query)
        testDispatcher.scheduler.advanceUntilIdle() // Execute the coroutine

        // Assert
        verify(mockMovieRepository).searchMovies(query, type) // Ensure the repository was called
        val uiState = viewModel.uiState.value
        assertTrue("UI State should be Success", uiState is SearchUiState.Success)
        assertEquals(fakeMovies, (uiState as SearchUiState.Success).movies)
    }

    @Test
    fun `searchMovies failure should update state to Error`() = runTest {
        // Arrange
        val query = "FailureQuery"
        val type = SearchType.TITLE
        val errorMessage = "Network Error"
        whenever(mockMovieRepository.searchMovies(query, type)).thenReturn(Result.failure(Exception(errorMessage)))

        // Act
        viewModel.searchMovies(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val uiState = viewModel.uiState.value
        assertTrue("UI State should be Error", uiState is SearchUiState.Error)
        assertEquals(errorMessage, (uiState as SearchUiState.Error).message)
    }
}