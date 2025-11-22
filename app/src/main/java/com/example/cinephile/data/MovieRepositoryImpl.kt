package com.example.cinephile.data

import com.example.cinephile.BuildConfig
import com.example.cinephile.data.remote.MovieDto
import com.example.cinephile.data.remote.RetrofitClient
import com.example.cinephile.data.remote.TmdbApiService
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.domain.repository.MovieRepository
import com.example.cinephile.ui.search.SearchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MovieRepositoryImpl(
    private val apiService: TmdbApiService = RetrofitClient.apiService
) : MovieRepository {

    private val API_KEY = BuildConfig.TMDB_API_KEY
    private val posterBaseUrl = "https://image.tmdb.org/t/p/w500"

    // 1. SEARCH (Updated logic)
    override suspend fun searchMovies(query: String, type: SearchType): Result<List<Movie>> {
        return withContext(Dispatchers.IO) {
            try {
                if (query.isBlank()) return@withContext Result.success(emptyList())

                val response = when (type) {
                    SearchType.YEAR -> apiService.discoverMovies(apiKey = API_KEY, year = query)
                    SearchType.TITLE -> {
                        // Smart Search logic
                        val yearRegex = Regex("(.*)\\s(\\d{4})$")
                        val match = yearRegex.find(query)
                        if (match != null) {
                            apiService.searchMovies(apiKey = API_KEY, query = match.groupValues[1].trim(), year = match.groupValues[2])
                        } else {
                            apiService.searchMovies(apiKey = API_KEY, query = query)
                        }
                    }
                    // Default fallback
                    else -> apiService.searchMovies(apiKey = API_KEY, query = query)
                }

                val domainMovies = response.results.mapNotNull { it.toDomainModel() }
                Result.success(domainMovies)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // 2. TRENDING
    override suspend fun getTrendingMovies(): Result<List<Movie>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTrendingMovies(apiKey = API_KEY)
                Result.success(response.results.mapNotNull { it.toDomainModel() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // 3. RECOMMENDATIONS
    override suspend fun getRecommendedMovies(userId: Int): Result<List<Movie>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTopRatedMovies(apiKey = API_KEY)
                Result.success(response.results.mapNotNull { it.toDomainModel() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Helper Mapper
    private fun MovieDto.toDomainModel(): Movie? {
        if (this.posterPath == null) return null
        return Movie(
            id = this.id,
            title = this.title,
            posterUrl = "$posterBaseUrl${this.posterPath}",
            overview = this.overview ?: "No overview available.",
            releaseDate = this.releaseDate ?: "Unknown",
            rating = this.voteAverage ?: 0.0
        )
    }
}