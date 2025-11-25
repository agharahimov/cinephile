package com.example.cinephile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val posterPath: String?,
    val overview: String,
    val backdropPath: String?,
    val releaseDate: String,
    val isInWatchlist: Boolean = false,
    val isLiked: Boolean = false,
    val voteAverage: Double = 0.0
)