package com.example.cinephile.domain.repository

import com.example.cinephile.data.local.UserListEntity
import com.example.cinephile.domain.model.Quiz

interface QuizRepository {
    suspend fun getAllLists(): Result<List<UserListEntity>>

    suspend fun generateQuizFromList(listId: Long, listName: String): Result<Quiz>
}