package com.example.cinephile.data.remote

// A simplified DTO for network responses from TMDB
data class MovieDto(
    val id: Int,
    val title: String,
    val poster_path: String?,
    val release_date: String
)