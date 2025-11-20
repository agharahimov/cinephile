package com.example.cinephile.domain.repository

import com.example.cinephile.domain.model.Movie

interface MovieRepository {
    suspend fun searchMovies(query: String): Result<List<Movie>>
}