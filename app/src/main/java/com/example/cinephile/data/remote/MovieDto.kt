package com.example.cinephile.data.remote

import com.google.gson.annotations.SerializedName

// 1. Search/Trending Response (List)
data class MovieResponseDto(
    @SerializedName("results") val results: List<MovieDto>
)

// 2. Basic Movie Info (For Lists)
data class MovieDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("genre_ids") val genreIds: List<Int>?
)

// 3. Detailed Movie Info (For Details Screen)
data class MovieDetailDto(
    val id: Int,
    val title: String,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,

    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    val runtime: Int?,
    val genres: List<GenreDto>?
)

data class GenreDto(val id: Int, val name: String)

// 4. Cast & Crew (For finding Director)
data class CreditsDto(
    val cast: List<CastDto>,
    val crew: List<CrewDto>
)

data class CastDto(
    val name: String,
    @SerializedName("character") val character: String,
    @SerializedName("profile_path") val profilePath: String?
)

data class CrewDto(
    val name: String,
    val job: String
)
data class PersonResponseDto(
    @SerializedName("results") val results: List<PersonDto>
)

data class PersonDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)