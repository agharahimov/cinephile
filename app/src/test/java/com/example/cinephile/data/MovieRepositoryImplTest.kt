package com.example.cinephile.data

import com.example.cinephile.data.remote.RetrofitClient
import com.example.cinephile.data.remote.TmdbApiService
import com.example.cinephile.ui.search.SearchType
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MovieRepositoryImplTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: TmdbApiService
    private lateinit var repository: MovieRepositoryImpl

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/")) // Use the mock server's URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApiService::class.java)

        // We inject our mocked apiService into the repository
        repository = MovieRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `searchMovies success should return a list of mapped movies`() = runTest {
        // Arrange: Create a fake JSON response that mimics the TMDB API
        val fakeJsonResponse = """
        {
            "results": [
                {
                    "id": 27205,
                    "title": "Inception",
                    "poster_path": "/9gk7adHYeDvHkCK_v5S_aiM8tC.jpg",
                    "overview": "A thief who steals corporate secrets...",
                    "release_date": "2010-07-15"
                },
                {
                    "id": 157336,
                    "title": "Interstellar",
                    "poster_path": null, 
                    "overview": "A team of explorers travel through a wormhole...",
                    "release_date": "2014-11-05"
                }
            ]
        }
        """.trimIndent()

        // Tell the mock server to return this fake response when a request comes in
        mockWebServer.enqueue(MockResponse().setBody(fakeJsonResponse).setResponseCode(200))

        // Act: Call the function we want to test
        val result = repository.searchMovies("any query", SearchType.TITLE)

        // Assert: Check the outcome
        assertTrue(result.isSuccess)
        val movies = result.getOrNull()
        assertNotNull(movies)

        // We expect only 1 movie because "Interstellar" has a null posterPath
        // and our mapping logic is designed to filter it out.
        assertEquals(1, movies?.size)
        assertEquals("Inception", movies?.get(0)?.title)
        assertTrue(movies?.get(0)?.posterUrl?.endsWith("/9gk7adHYeDvHkCK_v5S_aiM8tC.jpg") ?: false)
    }

    @Test
    fun `searchMovies API error should return failure`() = runTest {
        // Arrange: Tell the mock server to respond with a 404 Not Found error
        mockWebServer.enqueue(MockResponse().setResponseCode(404))

        // Act
        val result = repository.searchMovies("any query", SearchType.TITLE)

        // Assert
        assertTrue(result.isFailure)
    }
}