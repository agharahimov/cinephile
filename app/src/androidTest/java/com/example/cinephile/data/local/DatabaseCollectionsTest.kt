package com.example.cinephile.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseCollectionsTest {

    private lateinit var db: AppDatabase
    private lateinit var movieDao: MovieDao
    private lateinit var userListDao: UserListDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Use an in-memory database for testing so it's fresh for each test run.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        movieDao = db.movieDao()
        userListDao = db.userListDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndRetrieveCollections_worksCorrectly() = runBlocking {
        // Arrange: Create a sample movie and a sample custom list
        val testMovie = MovieEntity(123, "Test Movie", "/path.jpg", "test overview", "/path2.jpg", "2012",
            isInWatchlist = false,  isLiked = false)
        val testList = UserListEntity(name = "My Test List")

        // Act Part 1: Insert data
        movieDao.insertOrUpdateMovie(testMovie)
        val newListId = userListDao.createList(testList) // Get the ID of the new list

        // Act Part 2: Update statuses and add to list
        movieDao.setMovieWatchlistStatus(movieId = 123, inWatchlist = true)
        movieDao.setMovieLikedStatus(movieId = 123, isLiked = true)
        userListDao.addMovieToList(UserListMovieCrossRef(listId = newListId, movieId = 123))

        // Assert Part 1: Check simple properties
        val retrievedMovie = movieDao.getMovieById(123)
        assertNotNull(retrievedMovie)
        assertTrue(retrievedMovie!!.isInWatchlist)
        assertTrue(retrievedMovie.isLiked)

        // Assert Part 2: Check collections
        val watchlistMovies = movieDao.getWatchlistMovies()
        val likedMovies = movieDao.getLikedMovies()
        val customListMovies = userListDao.getMoviesForList(newListId)

        assertEquals(1, watchlistMovies.size)
        assertEquals("Test Movie", watchlistMovies[0].title)

        assertEquals(1, likedMovies.size)
        assertEquals(1, customListMovies.size)
    }
}