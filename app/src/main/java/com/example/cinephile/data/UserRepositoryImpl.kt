package com.example.cinephile.data

import com.example.cinephile.data.local.UserDao
import com.example.cinephile.data.local.UserEntity
import com.example.cinephile.domain.repository.UserRepository

// This implementation only uses the local DAO for now
class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {

    override suspend fun login(email: String, password: String): Result<Int> {
        val user = userDao.getUserByEmail(email)
            ?: return Result.failure(Exception("User not found"))

        // FAKE password check. In a real app, you'd compare hashes.
        if (user.passwordHash == password) {
            return Result.success(user.id)
        } else {
            return Result.failure(Exception("Invalid password"))
        }
    }

    override suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            // FAKE hashing. In a real app, use a proper hashing library like BCrypt.
            val newUser = UserEntity(email = email, passwordHash = password)
            userDao.insertUser(newUser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}