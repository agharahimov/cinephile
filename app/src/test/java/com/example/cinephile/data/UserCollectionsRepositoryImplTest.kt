package com.example.cinephile.data

import com.example.cinephile.data.local.MovieDao
import com.example.cinephile.data.local.MovieEntity
import com.example.cinephile.data.local.UserListDao
import com.example.cinephile.data.local.UserListMovieCrossRef
import com.example.cinephile.domain.model.Movie
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertFalse

class UserCollectionsRepositoryImplTest {

    private lateinit var mockMovieDao: MovieDao
    private lateinit var mockUserListDao: UserListDao
    private lateinit var repository: UserCollectionsRepositoryImpl

    // A sample domain model Movie for use in tests
    private val testDomainMovie = Movie(123, "Test Movie", "https://image.tmdb.org/t/p/w500/poster.jpg", "overview", "2025")

    @Before
    fun setUp() {
        mockMovieDao = mock()
        mockUserListDao = mock()
        repository = UserCollectionsRepositoryImpl(mockMovieDao, mockUserListDao)
    }

    @Test
    fun `addMovieToWatchlist should call insertOrUpdateMovie with isInWatchlist flag true`() = runTest {
        // Arrange
        val captor = argumentCaptor<MovieEntity>()
        // Simulate that the movie does not exist in the DB yet
        whenever(mockMovieDao.getMovieById(testDomainMovie.id)).thenReturn(null)

        // Act
        repository.addMovieToWatchlist(testDomainMovie)

        // Assert: Verify that the final "insert/update" call was made and capture the entity
        verify(mockMovieDao).insertOrUpdateMovie(captor.capture())

        // Check the captured entity's properties
        val capturedEntity = captor.firstValue
        assertEquals(testDomainMovie.id, capturedEntity.id)
        assertTrue("isInWatchlist should be set to true", capturedEntity.isInWatchlist)
        assertFalse("isLiked should remain the default false", capturedEntity.isLiked)
    }

    @Test
    fun `likeMovie should call insertOrUpdateMovie with isLiked flag true`() = runTest {
        // Arrange
        val captor = argumentCaptor<MovieEntity>()
        // Simulate that the movie already exists, but is not liked
        val existingEntity = MovieEntity(id=testDomainMovie.id, title=testDomainMovie.title, posterPath="poster.jpg", releaseDate=testDomainMovie.releaseDate, isInWatchlist = true, isLiked = false)
        whenever(mockMovieDao.getMovieById(testDomainMovie.id)).thenReturn(existingEntity)

        // Act
        repository.likeMovie(testDomainMovie)

        // Assert
        verify(mockMovieDao).insertOrUpdateMovie(captor.capture())

        val capturedEntity = captor.firstValue
        assertEquals(testDomainMovie.id, capturedEntity.id)
        assertTrue("isLiked should now be set to true", capturedEntity.isLiked)
        assertTrue("isInWatchlist should be preserved as true", capturedEntity.isInWatchlist)
    }

    @Test
    fun `getWatchlist should fetch from DAO and map to domain model`() = runTest {
        // Arrange: Tell the mock DAO what to return when getWatchlistMovies() is called
        val fakeMovieEntity = MovieEntity(123, "Test Movie", "/poster.jpg", "2025", isInWatchlist = true)
        whenever(mockMovieDao.getWatchlistMovies()).thenReturn(listOf(fakeMovieEntity))

        // Act
        val result = repository.getWatchlist()

        // Assert
        assertTrue(result.isSuccess)
        val movies = result.getOrNull()
        assertEquals(1, movies?.size)
        assertEquals("Test Movie", movies?.get(0)?.title)
        // Check that the poster path was correctly converted back to a full URL
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", movies?.get(0)?.posterUrl)
    }

    @Test
    fun `createCustomList should call DAO and return new list ID`() = runTest {
        // Arrange
        val listName = "My Awesome List"
        val expectedNewId = 1L
        // Tell the mock DAO to return our expected ID when createList is called
        whenever(mockUserListDao.createList(any())).thenReturn(expectedNewId)

        // Act
        val result = repository.createCustomList(listName)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedNewId, result.getOrNull())
        // Verify that the createList method on the DAO was actually called
        verify(mockUserListDao).createList(any())
    }

    @Test
    fun `addMovieToCustomList should call DAO with correct join object`() = runTest {
        // Arrange
        val movieId = 123
        val listId = 1L
        val captor = argumentCaptor<UserListMovieCrossRef>() // Use a captor to inspect the object

        // Act
        repository.addMovieToCustomList(movieId, listId)

        // Assert
        // Verify addMovieToList was called, and capture the object that was passed to it
        verify(mockUserListDao).addMovieToList(captor.capture())
        // Check that the captured object has the correct IDs
        assertEquals(listId, captor.firstValue.listId)
        assertEquals(movieId, captor.firstValue.movieId)
    }
}