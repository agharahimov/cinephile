package com.example.cinephile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String, // Added this because your UI has a Username field
    val email: String,
    val password: String // Your repo called this 'passwordHash', but let's keep it simple
)