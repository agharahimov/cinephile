package com.example.cinephile

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cinephile.data.MovieRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LiveMovieApiTest {

    @Test
    fun searchMovies_withValidQuery_shouldReturnSuccessAndData() {
        // Arrange: Instantiate the REAL repository
        val repository = MovieRepositoryImpl()
        val query = "Inception"

        // Act: We use runBlocking because this is a test, not main app code
        val result = runBlocking {
            repository.searchMovies(query)
        }

        // Assert: Check that the call was successful
        assertTrue("API call failed: ${result.exceptionOrNull()?.message}", result.isSuccess)

        val movies = result.getOrNull()
        assertNotNull("The movie list should not be null", movies)
        assertNotEquals("The movie list should not be empty", 0, movies?.size)

        // The most important part: SEEING the result
        Log.d("LiveApiTest", "Found ${movies?.size} movies for query '$query'.")
        Log.d("LiveApiTest", "First result: ${movies?.firstOrNull()}")

        // Assert that we probably got the right movie
        assertTrue(movies?.any { it.title.contains("Inception", ignoreCase = true) } ?: false)
    }
}