package com.example.cinephile.data.local

import androidx.room.Entity

// This links a UserListEntity with a MovieEntity
@Entity(tableName = "user_list_movie_cross_ref", primaryKeys = ["listId", "movieId"])
data class UserListMovieCrossRef(
    val listId: Long,
    val movieId: Int // This corresponds to the 'id' field in MovieEntity
)