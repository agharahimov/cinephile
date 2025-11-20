package com.example.cinephile.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.cinephile.data.local.UserEntity
import com.example.cinephile.domain.repository.UserRepository
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
class AuthViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule() // For LiveData

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
    fun `login with correct credentials should post LoginSuccess state`() = runTest {
        // Arrange
        val email = "test@test.com"
        val password = "password123"
        val fakeUser = UserEntity(id = 1, username = "test", email = email, password = password)
        // THIS LINE IS NOW CORRECT: login returns Result<UserEntity>
        whenever(mockUserRepository.login(email, password)).thenReturn(Result.success(fakeUser))

        // Act
        viewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        verify(mockUserRepository).login(email, password)
        val authState = viewModel.authState.value
        // THIS LINE IS NOW CORRECT: It checks for LoginSuccess
        assertTrue("AuthState should be LoginSuccess", authState is AuthState.LoginSuccess)
        assertEquals(fakeUser, (authState as AuthState.LoginSuccess).user)
    }

    @Test
    fun `register with new details should post RegistrationSuccess state`() = runTest {
        // Arrange
        val username = "newUser"
        val email = "new@test.com"
        val password = "new_pass_456"
        // THIS LINE IS NOW CORRECT: register returns Result<Unit>
        whenever(mockUserRepository.register(username, email, password)).thenReturn(Result.success(Unit))

        // Act
        viewModel.register(username, email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        verify(mockUserRepository).register(username, email, password)
        val authState = viewModel.authState.value
        // THIS LINE IS NOW CORRECT: It checks for RegistrationSuccess
        assertTrue("AuthState should be RegistrationSuccess", authState is AuthState.RegistrationSuccess)
    }
}