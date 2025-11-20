package com.example.cinephile.ui.auth

import com.example.cinephile.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private lateinit var mockUserRepository: UserRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockUserRepository = mock()
        viewModel = AuthViewModel(mockUserRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login success should update authState to Authenticated`() = runTest {
        // Arrange
        val email = "test@test.com"
        val password = "password123"
        // We tell the mock repository to return a success result when 'login' is called
        whenever(mockUserRepository.login(email, password)).thenReturn(Result.success(1))

        // Act
        viewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle() // Run the coroutine

        // Assert
        // Verify that the repository's login function was actually called with the correct data
        verify(mockUserRepository).login(email, password)
        // Check that the UI state has been updated to Authenticated
        assertTrue(viewModel.authState.value is AuthState.Authenticated)
    }
}