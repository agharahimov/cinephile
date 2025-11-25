package com.example.cinephile.data

import com.example.cinephile.data.local.MovieDao
import com.example.cinephile.data.local.MovieEntity
import com.example.cinephile.data.local.UserListDao
import com.example.cinephile.data.local.UserListEntity
import com.example.cinephile.data.local.UserListMovieCrossRef
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.domain.repository.UserCollectionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserCollectionsRepositoryImpl(
    private val movieDao: MovieDao,
    private val userListDao: UserListDao
) : UserCollectionsRepository {

    // --- Watchlist Impl ---
    override suspend fun addMovieToWatchlist(movie: Movie) = withContext(Dispatchers.IO) {
        // 1. Check if the movie already exists in the database
        val existingEntity = movieDao.getMovieById(movie.id)

        val entityToInsert = if (existingEntity != null) {
            // 2a. If it exists, create a copy but set isInWatchlist to true
            existingEntity.copy(isInWatchlist = true)
        } else {
            // 2b. If it's new, convert the domain model to an entity and set isInWatchlist to true
            movie.toEntity().copy(isInWatchlist = true)
        }

        // 3. Insert the final, correct entity in a single operation
        movieDao.insertOrUpdateMovie(entityToInsert)
    }

    override suspend fun removeMovieFromWatchlist(movieId: Int) = withContext(Dispatchers.IO) {
        movieDao.setMovieWatchlistStatus(movieId, inWatchlist = false)
    }

    override suspend fun getWatchlist(): Result<List<Movie>> = withContext(Dispatchers.IO) {
        try {
            val movieEntities = movieDao.getWatchlistMovies()
            Result.success(movieEntities.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isMovieInWatchlist(movieId: Int): Boolean = withContext(Dispatchers.IO) {
        movieDao.getMovieById(movieId)?.isInWatchlist ?: false
    }

    // --- Liked Movies Impl ---
    override suspend fun likeMovie(movie: Movie) = withContext(Dispatchers.IO) {
        val existingEntity = movieDao.getMovieById(movie.id)

        val entityToInsert = if (existingEntity != null) {
            existingEntity.copy(isLiked = true)
        } else {
            movie.toEntity().copy(isLiked = true)
        }

        movieDao.insertOrUpdateMovie(entityToInsert)
    }

    override suspend fun unlikeMovie(movieId: Int) = withContext(Dispatchers.IO) {
        movieDao.setMovieLikedStatus(movieId, isLiked = false)
    }

    override suspend fun getLikedMovies(): Result<List<Movie>> = withContext(Dispatchers.IO) {
        try {
            val movieEntities = movieDao.getLikedMovies()
            Result.success(movieEntities.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isMovieLiked(movieId: Int): Boolean = withContext(Dispatchers.IO) {
        movieDao.getMovieById(movieId)?.isLiked ?: false
    }

    // --- Custom Lists Impl ---
    // ... inside the UserCollectionsRepositoryImpl class ...

// --- CUSTOM LISTS IMPLEMENTATION ---

    override suspend fun createCustomList(name: String): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (name.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("List name cannot be blank."))
            }
            val newList = UserListEntity(name = name)
            val newListId = userListDao.createList(newList)
            Result.success(newListId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addMovieToCustomList(movieId: Int, listId: Long) = withContext(Dispatchers.IO) {
        val join = UserListMovieCrossRef(listId = listId, movieId = movieId)
        userListDao.addMovieToList(join)
    }

    override suspend fun getAllCustomLists(): Result<List<UserListEntity>> = withContext(Dispatchers.IO) {
        try {
            Result.success(userListDao.getAllLists())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMoviesInCustomList(listId: Long): Result<List<Movie>> = withContext(Dispatchers.IO) {
        try {
            val movieEntities = userListDao.getMoviesForList(listId)
            Result.success(movieEntities.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}

// --- Mapper Functions ---
// These private functions help convert between your Domain models and your Entity models.

private fun Movie.toEntity(
    isInWatchlist: Boolean? = null,
    isLiked: Boolean? = null
): MovieEntity {
    return MovieEntity(
        id = this.id,
        title = this.title,
        posterPath = this.posterUrl, // Extract path from full URL
        overview = this.overview,
        backdropPath = this.backdropUrl,
        releaseDate = this.releaseDate,
        voteAverage = this.rating,
        isInWatchlist = isInWatchlist ?: false,
        isLiked = isLiked ?: false
    )
}

private fun MovieEntity.toDomainModel(): Movie {
    // Base URLs as backup
    val posterBase = "https://image.tmdb.org/t/p/w500"
    val backdropBase = "https://image.tmdb.org/t/p/w780"

    // 1. Smart URL Fix (Handles both full links and partial paths)
    val fixedPosterUrl = if (this.posterPath?.startsWith("http") == true) {
        this.posterPath
    } else {
        "$posterBase${this.posterPath}"
    }

    val fixedBackdropUrl = if (this.backdropPath?.startsWith("http") == true) {
        this.backdropPath
    } else {
        "$backdropBase${this.backdropPath}"
    }

    return Movie(
        id = this.id,
        title = this.title,
        posterUrl = fixedPosterUrl ?: "",
        backdropUrl = fixedBackdropUrl ?: "",
        overview = "",
        releaseDate = this.releaseDate,

        // 2. RESTORE RATING (Fixes 0.0)
        rating = this.voteAverage,

        director = ""
    )
}