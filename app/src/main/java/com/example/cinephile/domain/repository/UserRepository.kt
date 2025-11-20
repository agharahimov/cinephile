package com.example.cinephile.domain.repository

import com.example.cinephile.data.local.UserEntity

interface UserRepository {
    // Returns the whole User object on success, or fails
    suspend fun login(email: String, pass: String): Result<UserEntity>

    // Takes username, email, and password
    suspend fun register(username: String, email: String, pass: String): Result<Unit>

    suspend fun userExists(email: String): Boolean
}