package com.example.cinephile.data

import com.example.cinephile.data.local.MovieDao
import com.example.cinephile.data.local.MovieEntity
import com.example.cinephile.data.local.UserListDao
import com.example.cinephile.data.local.UserListEntity
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.domain.model.Quiz
import com.example.cinephile.domain.quiz.QuizGenerator
import com.example.cinephile.domain.repository.QuizRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuizRepositoryImpl(private val movieDao: MovieDao,private val userListDao: UserListDao) : QuizRepository {

    private val generator = QuizGenerator()

    // 1. Get all lists for the selection screen
    override suspend fun getAllLists(): Result<List<UserListEntity>> = withContext(Dispatchers.IO) {
        try {
            // Ensure default list exists (just in case)
            val lists = userListDao.getAllLists()
            Result.success(lists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. Generate quiz from specific list
    override suspend fun generateQuizFromList(listId: Long, listName: String): Result<Quiz> = withContext(Dispatchers.IO) {
        try {
            // Use the new DAO method to get movies for this specific list
            val entities = userListDao.getMoviesForList(listId)

            // Business Rule: Need at least 4 movies to make a good quiz
            if (entities.size < 4) {
                return@withContext Result.failure(Exception("List '$listName' needs at least 4 movies to play."))
            }

            val movies = entities.map { it.toDomainModel() }

            // Generate the quiz
            val quiz = generator.generateQuizFromMovies("Quiz: $listName", movies)
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
            backdropUrl = this.backdropPath ?: this.posterPath ?: "",
            releaseDate = this.releaseDate
        )
    }
}