package com.example.cinephile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createList(list: UserListEntity): Long

    @Query("SELECT * FROM user_lists ORDER BY name ASC")
    suspend fun getAllLists(): List<UserListEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMovieToList(join: UserListMovieCrossRef)

    @Query("SELECT * FROM movies WHERE id IN (SELECT movieId FROM user_list_movie_cross_ref WHERE listId = :listId)")
    suspend fun getMoviesForList(listId: Long): List<MovieEntity>
}