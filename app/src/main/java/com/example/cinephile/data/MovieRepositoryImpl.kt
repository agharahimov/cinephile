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
    private val backdropBaseUrl = "https://image.tmdb.org/t/p/w780" // Wider quality for backdrops

    // 1. SEARCH
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

    // 4. GET MOVIE DETAILS (Updated for Letterboxd Style)
    override suspend fun getMovieDetails(movieId: Int): Result<Movie> {
        return try {
            // 1. Call API for Details
            val details = apiService.getMovieDetails(movieId, API_KEY)

            // 2. Call API for Director/Cast
            val credits = apiService.getMovieCredits(movieId, API_KEY)

            // 3. Extract Director
            val director = credits.crew.find { it.job == "Director" }?.name ?: "Unknown"

            // 4. Extract Cast (Top 3)
            val cast = credits.cast.take(3).joinToString(", ") { it.name }

            // 5. Convert to Domain Movie
            val movie = Movie(
                id = details.id,
                title = details.title,
                posterUrl = "$posterBaseUrl${details.posterPath}",

                // --- NEW: Map the Backdrop URL (Horizontal Image) ---
                backdropUrl = if (details.backdropPath != null)
                    "$backdropBaseUrl${details.backdropPath}"
                else
                    "$posterBaseUrl${details.posterPath}", // Fallback to poster if no backdrop

                // We combine the overview with the extra info for now
                overview = "${details.overview}\n\nDirector: $director\nCast: $cast",
                releaseDate = details.releaseDate ?: "",
                rating = details.voteAverage ?: 0.0
            )
            Result.success(movie)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Helper Mapper (Updated for Backdrop)
    private fun MovieDto.toDomainModel(): Movie? {
        if (this.posterPath == null) return null
        return Movie(
            id = this.id,
            title = this.title,
            posterUrl = "$posterBaseUrl${this.posterPath}",
            backdropUrl = if (this.backdropPath != null)
                "$backdropBaseUrl${this.backdropPath}"
            else
                "$posterBaseUrl${this.posterPath}",
            overview = this.overview ?: "No overview available.",
            releaseDate = this.releaseDate ?: "Unknown",
            rating = this.voteAverage ?: 0.0,
            genres = this.genreIds ?: emptyList()
        )
    }
}