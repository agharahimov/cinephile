package com.example.cinephile.domain.model

data class Quiz(
    val quizId: Long = 0,
    val name: String,
    val questions: List<Question>
)