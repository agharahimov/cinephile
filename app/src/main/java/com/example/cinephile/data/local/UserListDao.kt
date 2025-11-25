package com.example.cinephile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UserListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createList(list: UserListEntity): Long

    @Query("SELECT * FROM user_lists ORDER BY listId ASC")
    suspend fun getAllLists(): List<UserListEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMovieToList(join: UserListMovieCrossRef)

    @Query("SELECT * FROM movies WHERE id IN (SELECT movieId FROM user_list_movie_cross_ref WHERE listId = :listId)")
    suspend fun getMoviesForList(listId: Long): List<MovieEntity>



    // 1. Find which list is currently active
    @Query("SELECT * FROM user_lists WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentList(): UserListEntity?

    // 2. Helpers to switch the active list safely
    @Query("UPDATE user_lists SET isCurrent = 0")
    suspend fun clearCurrentFlags()

    @Query("UPDATE user_lists SET isCurrent = 1 WHERE listId = :listId")
    suspend fun setListAsCurrent(listId: Long)

    // 3. The Main Switch Function (Transaction ensures both happen together)
    @Transaction
    suspend fun updateCurrentList(listId: Long) {
        clearCurrentFlags()
        setListAsCurrent(listId)
    }
}