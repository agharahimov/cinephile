package com.example.cinephile.data

import com.example.cinephile.data.local.UserDao
import com.example.cinephile.data.local.UserEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserRepositoryImplTest {

    private lateinit var mockUserDao: UserDao
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        mockUserDao = mock()
        repository = UserRepositoryImpl(mockUserDao)
    }

    // Test for a successful login
    @Test
    fun `login success when user exists and password matches`() = runTest {
        // Arrange: Create a user entity that matches the new structure
        val testUser = UserEntity(id = 1, username = "testuser", email = "test@test.com", password = "password123")
        whenever(mockUserDao.getUserByEmail("test@test.com")).thenReturn(testUser)

        // Act: Attempt to log in with the correct credentials
        val result = repository.login("test@test.com", "password123")

        // Assert: The result should be a success and contain the user entity
        assertTrue(result.isSuccess)
        assertEquals(testUser, result.getOrNull())
    }

    // Test for a login failure due to wrong password
    @Test
    fun `login failure when password does not match`() = runTest {
        // Arrange
        val testUser = UserEntity(id = 1, username = "testuser", email = "test@test.com", password = "password123")
        whenever(mockUserDao.getUserByEmail("test@test.com")).thenReturn(testUser)

        // Act
        val result = repository.login("test@test.com", "wrong_password")

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Invalid password", result.exceptionOrNull()?.message)
    }

    // Test for a successful registration
    @Test
    fun `register success should insert a user with correct details`() = runTest {
        // Arrange
        val captor = argumentCaptor<UserEntity>() // Captor to inspect the object passed to the DAO

        // Act
        val result = repository.register("New User", "new@test.com", "new_pass_456")

        // Assert
        assertTrue(result.isSuccess)
        // Verify that insertUser was called, and capture the UserEntity that was passed to it
        verify(mockUserDao).insertUser(captor.capture())
        // Check that the captured entity has the correct details
        assertEquals("New User", captor.firstValue.username)
        assertEquals("new@test.com", captor.firstValue.email)
        assertEquals("new_pass_456", captor.firstValue.password)
    }
}