package com.example.cinephile.domain.repository

import com.example.cinephile.domain.model.Quiz

interface QuizRepository {
    suspend fun generateWatchlistQuiz(): Result<Quiz>
}