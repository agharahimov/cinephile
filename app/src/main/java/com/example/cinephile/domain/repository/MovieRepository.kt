package com.example.cinephile.domain.repository

import com.example.cinephile.domain.model.Movie
import com.example.cinephile.ui.search.SearchType // <--- IMPORTANT IMPORT

interface MovieRepository {
    // 1. Search with Type (Title, Year, etc.)
    suspend fun searchMovies(query: String, type: SearchType): Result<List<Movie>>

    // 2. Home Screen Trending
    suspend fun getTrendingMovies(): Result<List<Movie>>

    // 3. Recommendations (Placeholder)
    suspend fun getRecommendedMovies(userId: Int): Result<List<Movie>>
    suspend fun getMovieDetails(movieId: Int): Result<Movie>
}