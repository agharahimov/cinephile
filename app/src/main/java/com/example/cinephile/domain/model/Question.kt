package com.example.cinephile.domain.model

enum class QuestionType {
    RELEASE_YEAR,
    GUESS_FROM_PLOT,
    GUESS_FROM_POSTER
}

data class Question(
    val type: QuestionType,
    val questionText: String, // The main text to display (e.g. "Which movie...")
    val imageUrl: String? = null, // Used only for GUESS_FROM_POSTER
    val options: List<String>, // The 4 possible answers
    val correctAnswer: String, // The correct string from the options
    val correctMovie: Movie // Keep reference to the movie for UI context if needed
)