package com.example.cinephile.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApiService {

    // 1. Search (UPDATED: Added 'year' parameter for Smart Search)
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("api_key") apiKey: String,
        @Query("primary_release_year") year: String? = null // <--- Added this to fix "No parameter 'year' found"
    ): MovieSearchResponse

    // 2. Trending Movies (Existing)
    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String
    ): MovieSearchResponse

    // 3. Top Rated Movies (Existing)
    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String
    ): MovieSearchResponse

    // ---------------------------------------------------------
    // NEW METHODS REQUIRED TO FIX YOUR BUILD ERRORS
    // ---------------------------------------------------------

    // 4. Discover (Fixes 'Unresolved reference: discoverMovies')
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("primary_release_year") year: String? = null,
        @Query("with_genres") genreId: String? = null,
        @Query("with_people") personId: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): MovieSearchResponse

    // 5. Search Person (Fixes 'Unresolved reference: searchPerson' for Directors)
    @GET("search/person")
    suspend fun searchPerson(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): PersonResponseDto

}

// --- ADD THESE DATA CLASSES AT THE BOTTOM SO 'searchPerson' WORKS ---
data class PersonResponseDto(
    val results: List<PersonDto>
)

data class PersonDto(
    val id: Int,
    val name: String
)