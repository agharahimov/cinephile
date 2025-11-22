package com.example.cinephile.domain.model

data class Question(
    val questionText: String,
    val options: List<String>, // e.g., ["1999", "2001", "2010", "2014"]
    val correctAnswer: String,
    val movieTitleHint: String // e.g., "for the movie Inception"
)