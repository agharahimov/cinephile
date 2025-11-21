package com.example.cinephile.data

import com.example.cinephile.BuildConfig
import com.example.cinephile.data.remote.MovieDto
import com.example.cinephile.data.remote.RetrofitClient
import com.example.cinephile.data.remote.TmdbApiService
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.domain.repository.MovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MovieRepositoryImpl (
    private val apiService: TmdbApiService = RetrofitClient.apiService
        ) : MovieRepository {

    private val posterBaseUrl = "https://image.tmdb.org/t/p/w500"

    override suspend fun searchMovies(query: String): Result<List<Movie>> {
        // Use withContext to ensure this network call runs on the IO dispatcher
        return withContext(Dispatchers.IO) {
            try {
                // Don't waste API calls on empty searches
                if (query.isBlank()) {
                    return@withContext Result.success(emptyList())
                }

                val response = apiService.searchMovies(query = query, apiKey = BuildConfig.TMDB_API_KEY)

                // Map the DTO list to our clean Domain Model list
                // Use mapNotNull to automatically filter out any movies that are invalid (e.g., no poster)
                val domainMovies = response.results.mapNotNull { dto ->
                    dto.toDomainModel()
                }

                Result.success(domainMovies)
            } catch (e: Exception) {
                // Catch any network or parsing errors
                Result.failure(e)
            }
        }
    }

    // This private extension function handles the mapping logic for a single item.
    private fun MovieDto.toDomainModel(): Movie? {
        // Business Rule: We don't want to display movies without a poster.
        // Returning null here will cause mapNotNull to exclude it from the final list.
        if (this.posterPath == null) {
            return null
        }

        return Movie(
            id = this.id,
            title = this.title,
            posterUrl = "$posterBaseUrl${this.posterPath}", // Build the full URL here!
            overview = this.overview ?: "No overview available.",
            releaseDate = this.releaseDate ?: "Unknown"
        )
    }
}