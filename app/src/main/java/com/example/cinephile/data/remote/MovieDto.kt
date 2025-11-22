package com.example.cinephile.data.remote

import com.google.gson.annotations.SerializedName

data class MovieSearchResponse(
    val results: List<MovieDto>
)
// A simplified DTO for network responses from TMDB
data class MovieDto(
    val id: Int,
    val title: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("vote_average") val voteAverage: Double?
)