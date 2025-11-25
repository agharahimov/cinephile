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

    // --- UPDATED: Add to Watchlist (Handles "Current List" Logic) ---
    override suspend fun addMovieToWatchlist(movie: Movie) = withContext(Dispatchers.IO) {
        // 1. Ensure movie exists in DB (Save/Update)
        // We preserve the existing state if it's already there
        val existingEntity = movieDao.getMovieById(movie.id)
        val entityToInsert = existingEntity?.copy(isInWatchlist = true)
            ?: movie.toEntity(isInWatchlist = true)

        movieDao.insertOrUpdateMovie(entityToInsert)

        // 2. Find the Current List (or create one if missing)
        var currentList = userListDao.getCurrentList()
        if (currentList == null) {
            // First time setup: Create default list
            val defaultId = userListDao.createList(UserListEntity(name = "My Watchlist", isCurrent = true))
            currentList = UserListEntity(defaultId, "My Watchlist", true)
        }

        // 3. Link Movie to the Current List
        val join = UserListMovieCrossRef(listId = currentList.listId, movieId = movie.id)
        userListDao.addMovieToList(join)
    }

    override suspend fun removeMovieFromWatchlist(movieId: Int) = withContext(Dispatchers.IO) {
        // We just toggle the flag off for now.
        movieDao.setMovieWatchlistStatus(movieId, inWatchlist = false)
    }

    override suspend fun getWatchlist(): Result<List<Movie>> = withContext(Dispatchers.IO) {
        try {
            // Fetches all movies marked as watchlist (Backward compatibility)
            val movieEntities = movieDao.getWatchlistMovies()
            Result.success(movieEntities.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isMovieInWatchlist(movieId: Int): Boolean = withContext(Dispatchers.IO) {
        movieDao.getMovieById(movieId)?.isInWatchlist ?: false
    }

    // --- Liked Movies Impl (Unchanged) ---
    override suspend fun likeMovie(movie: Movie) = withContext(Dispatchers.IO) {
        val existingEntity = movieDao.getMovieById(movie.id)
        val entityToInsert = existingEntity?.copy(isLiked = true)
            ?: movie.toEntity(isLiked = true)
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

    // --- CUSTOM LISTS IMPLEMENTATION (Unchanged) ---

    override suspend fun createCustomList(name: String): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (name.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("List name cannot be blank."))
            }
            // New lists are NOT current by default
            val newList = UserListEntity(name = name, isCurrent = false)
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

    // --- NEW LOGIC: List Management Implementation ---

    override suspend fun setCurrentList(listId: Long) = withContext(Dispatchers.IO) {
        userListDao.updateCurrentList(listId)
    }

    override suspend fun getCurrentList(): UserListEntity? = withContext(Dispatchers.IO) {
        userListDao.getCurrentList()
    }

    override suspend fun ensureDefaultListExists() = withContext(Dispatchers.IO) {
        val lists = userListDao.getAllLists()
        if (lists.isEmpty()) {
            userListDao.createList(UserListEntity(name = "My Watchlist", isCurrent = true))
        }
    }
}

// --- Mapper Functions (Kept exactly as requested) ---

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