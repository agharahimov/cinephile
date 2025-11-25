package com.example.cinephile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_lists")
data class UserListEntity(
    @PrimaryKey(autoGenerate = true) val listId: Long = 0,
    val name: String,
    val isCurrent: Boolean = false
)