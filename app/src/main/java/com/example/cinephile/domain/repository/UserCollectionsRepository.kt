package com.example.cinephile.domain.repository

import com.example.cinephile.data.local.UserListEntity
import com.example.cinephile.domain.model.Movie

// This interface defines all the actions related to a user's movie collections.
interface UserCollectionsRepository {

    // --- Watchlist Functions ---
    suspend fun addMovieToWatchlist(movie: Movie)
    suspend fun removeMovieFromWatchlist(movieId: Int)
    suspend fun getWatchlist(): Result<List<Movie>>
    suspend fun isMovieInWatchlist(movieId: Int): Boolean
    suspend fun setCurrentList(listId: Long)
    suspend fun getCurrentList(): UserListEntity?

    suspend fun deleteUserList(listId: Long)
    suspend fun ensureDefaultListExists()
    suspend fun removeMovieFromList(movieId: Int, listId: Long)
    suspend fun renameUserList(listId: Long, newName: String)



    // --- Liked Movie Functions ---
    suspend fun likeMovie(movie: Movie)
    suspend fun unlikeMovie(movieId: Int)
    suspend fun getLikedMovies(): Result<List<Movie>>
    suspend fun isMovieLiked(movieId: Int): Boolean

    // --- Custom Lists ---
    suspend fun createCustomList(name: String): Result<Long>
    suspend fun addMovieToCustomList(movieId: Int, listId: Long)
    suspend fun getAllCustomLists(): Result<List<UserListEntity>> // We can create a domain model for this later
    suspend fun getMoviesInCustomList(listId: Long): Result<List<Movie>>
}