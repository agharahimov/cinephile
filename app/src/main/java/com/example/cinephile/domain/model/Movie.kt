package com.example.cinephile.domain.model

/**
 * Represents a movie in the domain layer. This is the clean model
 * that the UI and ViewModels will interact with.
 */
data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String,
    val overview: String,
    val releaseDate: String
)