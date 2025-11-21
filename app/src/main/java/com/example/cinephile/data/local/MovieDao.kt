package com.example.cinephile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface MovieDao {
    // Inserts a movie if it's new, or updates its details if it already exists.
    // Crucially, it will NOT overwrite the isLiked or isInWatchlist flags.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMovie(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): MovieEntity?

    @Query("UPDATE movies SET isInWatchlist = :inWatchlist WHERE id = :movieId")
    suspend fun setMovieWatchlistStatus(movieId: Int, inWatchlist: Boolean)

    @Query("SELECT * FROM movies WHERE isInWatchlist = 1")
    suspend fun getWatchlistMovies(): List<MovieEntity>

    @Query("UPDATE movies SET isLiked = :isLiked WHERE id = :movieId")
    suspend fun setMovieLikedStatus(movieId: Int, isLiked: Boolean)

    @Query("SELECT * FROM movies WHERE isLiked = 1")
    suspend fun getLikedMovies(): List<MovieEntity>
}