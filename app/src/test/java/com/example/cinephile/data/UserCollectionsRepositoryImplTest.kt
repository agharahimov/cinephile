package com.example.cinephile.data

import com.example.cinephile.data.local.MovieDao
import com.example.cinephile.data.local.MovieEntity
import com.example.cinephile.data.local.UserListDao
import com.example.cinephile.domain.model.Movie
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserCollectionsRepositoryImplTest {

    private lateinit var mockMovieDao: MovieDao
    private lateinit var mockUserListDao: UserListDao
    private lateinit var repository: UserCollectionsRepositoryImpl

    // This is the "domainMovie" variable. It serves as sample data for our tests.
    private val domainMovie = Movie(
        id = 1,
        title = "Test Movie",
        posterUrl = "https://image.tmdb.org/t/p/w500/poster.jpg",
        overview = "Test Overview",
        releaseDate = "2025"
    )

    @Before
    fun setUp() {
        mockMovieDao = mock()
        mockUserListDao = mock()
        repository = UserCollectionsRepositoryImpl(mockMovieDao, mockUserListDao)
    }

    @Test
    fun `addMovieToWatchlist (New Movie) should set isInWatchlist=true and isLiked=false`() = runTest {
        // Arrange: Simulate that the movie does NOT exist in the DB yet (returns null)
        whenever(mockMovieDao.getMovieById(1)).thenReturn(null)
        val captor = argumentCaptor<MovieEntity>()

        // Act
        repository.addMovieToWatchlist(domainMovie)

        // Assert
        verify(mockMovieDao).insertOrUpdateMovie(captor.capture())
        val savedMovie = captor.firstValue

        assertEquals(1, savedMovie.id)
        assertTrue("Should be in watchlist", savedMovie.isInWatchlist)
        assertFalse("Should NOT be liked by default", savedMovie.isLiked)
        assertEquals("Test Overview", savedMovie.overview) // Verify overview is saved
    }

    @Test
    fun `addMovieToWatchlist (Existing Liked Movie) should set isInWatchlist=true and PRESERVE isLiked`() = runTest {
        // Arrange: Simulate that the movie IS already in the DB and is LIKED
        val existingEntity = MovieEntity(
            id = 1, title = "Test Movie", posterPath = "/poster.jpg", overview = "Overview", releaseDate = "2025",
            isInWatchlist = false,
            isLiked = true // It is already liked
        )
        whenever(mockMovieDao.getMovieById(1)).thenReturn(existingEntity)

        val captor = argumentCaptor<MovieEntity>()

        // Act
        repository.addMovieToWatchlist(domainMovie)

        // Assert
        verify(mockMovieDao).insertOrUpdateMovie(captor.capture())
        val savedMovie = captor.firstValue

        assertTrue("Watchlist should now be true", savedMovie.isInWatchlist)
        assertTrue("Liked status should be PRESERVED as true", savedMovie.isLiked)
    }

    @Test
    fun `likeMovie (Existing Watchlist Movie) should set isLiked=true and PRESERVE isInWatchlist`() = runTest {
        // Arrange: Simulate movie is in Watchlist but NOT Liked
        val existingEntity = MovieEntity(
            id = 1, title = "Test Movie", posterPath = "/poster.jpg", overview = "Overview", releaseDate = "2025",
            isInWatchlist = true, // It is in watchlist
            isLiked = false
        )
        whenever(mockMovieDao.getMovieById(1)).thenReturn(existingEntity)

        val captor = argumentCaptor<MovieEntity>()

        // Act
        repository.likeMovie(domainMovie)

        // Assert
        verify(mockMovieDao).insertOrUpdateMovie(captor.capture())
        val savedMovie = captor.firstValue

        assertTrue("Liked should now be true", savedMovie.isLiked)
        assertTrue("Watchlist status should be PRESERVED as true", savedMovie.isInWatchlist)
    }

    @Test
    fun `getWatchlist should fetch from DAO and map correctly`() = runTest {
        // Arrange
        val dbEntity = MovieEntity(
            id = 1, title = "Test Movie", posterPath = "/poster.jpg", overview = "Overview", releaseDate = "2025",
            isInWatchlist = true, isLiked = false
        )
        whenever(mockMovieDao.getWatchlistMovies()).thenReturn(listOf(dbEntity))

        // Act
        val result = repository.getWatchlist()

        // Assert
        assertTrue(result.isSuccess)
        val list = result.getOrNull()
        assertEquals(1, list?.size)
        assertEquals("Test Movie", list?.get(0)?.title)
        // Verify full URL reconstruction
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", list?.get(0)?.posterUrl)
    }
}