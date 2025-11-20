import com.example.cinephile.data.UserRepositoryImpl
import com.example.cinephile.data.local.UserDao
import com.example.cinephile.data.local.UserEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class UserRepositoryImplTest {
    private lateinit var mockUserDao: UserDao
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        mockUserDao = mock()
        repository = UserRepositoryImpl(mockUserDao)
    }

    @Test
    fun `login success when user exists and password matches`() = runTest {
        // Arrange
        val testUser = UserEntity(id = 1, email = "test@test.com", passwordHash = "123456")
        whenever(mockUserDao.getUserByEmail("test@test.com")).thenReturn(testUser)

        // Act
        val result = repository.login("test@test.com", "123456")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())
    }
}