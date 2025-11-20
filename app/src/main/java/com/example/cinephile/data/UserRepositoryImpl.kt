package com.example.cinephile.data

import com.example.cinephile.data.local.UserDao
import com.example.cinephile.data.local.UserEntity
import com.example.cinephile.domain.repository.UserRepository

class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {

    override suspend fun login(email: String, pass: String): Result<UserEntity> {
        // 1. Find user by email
        val user = userDao.getUserByEmail(email)

        // 2. Check if user exists
        if (user == null) {
            return Result.failure(Exception("User not found"))
        }

        // 3. Check password
        if (user.password == pass) {
            return Result.success(user)
        } else {
            return Result.failure(Exception("Invalid password"))
        }
    }

    override suspend fun register(username: String, email: String, pass: String): Result<Unit> {
        return try {
            // Check if user already exists
            if (userDao.getUserByEmail(email) != null) {
                return Result.failure(Exception("Email already exists"))
            }

            // Create and Insert
            val newUser = UserEntity(
                username = username,
                email = email,
                password = pass
            )
            userDao.insertUser(newUser)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun userExists(email: String): Boolean {
        return userDao.getUserByEmail(email) != null
    }
}