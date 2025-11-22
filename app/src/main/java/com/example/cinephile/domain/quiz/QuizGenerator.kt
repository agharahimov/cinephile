package com.example.cinephile.domain.quiz

import com.example.cinephile.domain.model.Movie
import com.example.cinephile.domain.model.Question
import com.example.cinephile.domain.model.QuestionType
import com.example.cinephile.domain.model.Quiz

class QuizGenerator {

    fun generateQuizFromMovies(
        quizName: String,
        movies: List<Movie>,
        questionCount: Int = 10
    ): Quiz? {
        // Need at least 4 movies to generate meaningful multiple-choice options
        if (movies.size < 4) return null

        val questions = mutableListOf<Question>()
        // Shuffle movies to ensure random order
        val pool = movies.shuffled()

        // Loop through the pool to create questions
        // We cycle through the movies, creating one question per movie up to the limit
        for (i in 0 until minOf(pool.size, questionCount)) {
            val correctMovie = pool[i]

            // Randomly select a type, but ensure data exists for it
            val type = pickValidQuestionType(correctMovie)

            val question = createQuestion(correctMovie, type, movies)
            questions.add(question)
        }

        return Quiz(name = quizName, questions = questions)
    }

    private fun pickValidQuestionType(movie: Movie): QuestionType {
        val validTypes = mutableListOf<QuestionType>()

        // Always valid if we have a date
        if (movie.releaseDate.length >= 4) validTypes.add(QuestionType.RELEASE_YEAR)
        // Valid if we have a poster
        if (movie.posterUrl.isNotEmpty()) validTypes.add(QuestionType.GUESS_FROM_POSTER)
        // Valid if we have a decent overview
        if (movie.overview.length > 20) validTypes.add(QuestionType.GUESS_FROM_PLOT)

        // Default to Year if nothing else fits (shouldn't happen with valid data)
        return if (validTypes.isNotEmpty()) validTypes.random() else QuestionType.RELEASE_YEAR
    }

    private fun createQuestion(target: Movie, type: QuestionType, allMovies: List<Movie>): Question {
        // Get 3 random WRONG movies
        val wrongMovies = allMovies.filter { it.id != target.id }.shuffled().take(3)

        return when (type) {
            QuestionType.RELEASE_YEAR -> generateYearQuestion(target, wrongMovies)
            QuestionType.GUESS_FROM_PLOT -> generatePlotQuestion(target, wrongMovies)
            QuestionType.GUESS_FROM_POSTER -> generatePosterQuestion(target, wrongMovies)
        }
    }

    // --- ALGORITHM 1: Release Year ---
    private fun generateYearQuestion(target: Movie, wrongMovies: List<Movie>): Question {
        val correctYear = target.releaseDate.take(4)

        // Strategy: Try to make options confusing by picking years from other movies,
        // but if duplicates exist, generate random nearby years.
        val rawOptions = wrongMovies.map { it.releaseDate.take(4) }.toMutableList()
        rawOptions.add(correctYear)

        // Ensure uniqueness
        val finalOptions = rawOptions.distinct().toMutableList()
        while (finalOptions.size < 4) {
            // Add a random fake year close to the real one
            val fakeYear = (correctYear.toInt() + (-5..5).random()).toString()
            if (!finalOptions.contains(fakeYear)) finalOptions.add(fakeYear)
        }

        return Question(
            type = QuestionType.RELEASE_YEAR,
            questionText = "In which year was '${target.title}' released?",
            correctAnswer = correctYear,
            options = finalOptions.shuffled(),
            correctMovie = target
        )
    }

    // --- ALGORITHM 2: Guess from Plot ---
    private fun generatePlotQuestion(target: Movie, wrongMovies: List<Movie>): Question {
        // Hide the movie title if it appears in the overview!
        val obscuredOverview = target.overview.replace(target.title, "[THIS MOVIE]", ignoreCase = true)

        val options = (wrongMovies.map { it.title } + target.title).shuffled()

        return Question(
            type = QuestionType.GUESS_FROM_PLOT,
            questionText = "Which movie has this plot?\n\n\"$obscuredOverview\"",
            correctAnswer = target.title,
            options = options,
            correctMovie = target
        )
    }

    // --- ALGORITHM 3: Guess from Poster ---
    private fun generatePosterQuestion(target: Movie, wrongMovies: List<Movie>): Question {
        val options = (wrongMovies.map { it.title } + target.title).shuffled()

        return Question(
            type = QuestionType.GUESS_FROM_POSTER,
            questionText = "Identify the movie from this poster:",
            imageUrl = target.posterUrl,
            correctAnswer = target.title,
            options = options,
            correctMovie = target
        )
    }
}