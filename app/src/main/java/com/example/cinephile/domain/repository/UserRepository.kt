package com.example.cinephile.domain.repository

interface UserRepository {
    // Returns a user ID on success, or null on failure
    suspend fun login(email: String, password: String): Result<Int>
    suspend fun register(email: String, password: String): Result<Unit>
}