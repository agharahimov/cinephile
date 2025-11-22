package com.example.cinephile.data

import com.example.cinephile.data.local.MovieDao
import com.example.cinephile.data.local.MovieEntity
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.domain.model.Quiz
import com.example.cinephile.domain.quiz.QuizGenerator
import com.example.cinephile.domain.repository.QuizRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuizRepositoryImpl(private val movieDao: MovieDao) : QuizRepository {

    private val generator = QuizGenerator()

    override suspend fun generateWatchlistQuiz(): Result<Quiz> = withContext(Dispatchers.IO) {
        try {
            val entities = movieDao.getWatchlistMovies()

            // Business Rule: Need at least 4 movies to make a good quiz
            if (entities.size < 4) {
                return@withContext Result.failure(Exception("Not enough movies in watchlist (need at least 4)."))
            }

            val movies = entities.map { it.toDomainModel() }

            // Generate the quiz
            val quiz = generator.generateQuizFromMovies("Watchlist Challenge", movies)
                ?: return@withContext Result.failure(Exception("Failed to generate quiz."))

            Result.success(quiz)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper Mapper
    private fun MovieEntity.toDomainModel(): Movie {
        return Movie(
            id = this.id,
            title = this.title,
            // Reconstruct full URL if needed, or pass as is if already stored fully
            posterUrl = if (this.posterPath?.startsWith("http") == true) this.posterPath else "https://image.tmdb.org/t/p/w500${this.posterPath}",
            overview = this.overview,
            releaseDate = this.releaseDate
        )
    }
}