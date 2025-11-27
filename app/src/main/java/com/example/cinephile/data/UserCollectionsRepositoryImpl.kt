package com.example.cinephile.data

import android.util.Log
import com.example.cinephile.BuildConfig
import com.example.cinephile.data.local.MovieDao
import com.example.cinephile.data.local.MovieEntity
import com.example.cinephile.data.local.UserListDao
import com.example.cinephile.data.local.UserListEntity
import com.example.cinephile.data.local.UserListMovieCrossRef
import com.example.cinephile.data.remote.MovieDto
import com.example.cinephile.data.remote.RetrofitClient
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.domain.repository.UserCollectionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserCollectionsRepositoryImpl(
    private val movieDao: MovieDao,
    private val userListDao: UserListDao
) : UserCollectionsRepository {

    // Access API Service for Recommendations
    private val apiService = RetrofitClient.apiService

    // --- Watchlist Impl ---
    override suspend fun addMovieToWatchlist(movie: Movie) = withContext(Dispatchers.IO) {
        // 1. Upsert Movie to DB
        val existingEntity = movieDao.getMovieById(movie.id)
        val entityToInsert = if (existingEntity != null) {
            existingEntity.copy(isInWatchlist = true)
        } else {
            movie.toEntity().copy(isInWatchlist = true)
        }
        movieDao.insertOrUpdateMovie(entityToInsert)

        // 2. Add to Default List ("My Watchlist")
        // We ensure a default list exists so queries always work
        ensureDefaultListExists()
        val currentList = userListDao.getCurrentList()
            ?: userListDao.getAllLists().firstOrNull() // Fallback if no current set

        if (currentList != null) {
            val join = UserListMovieCrossRef(listId = currentList.listId, movieId = movie.id)
            userListDao.addMovieToList(join)
        }
    }

    override suspend fun removeMovieFromWatchlist(movieId: Int) = withContext(Dispatchers.IO) {
        // Remove from Default List
        val currentList = userListDao.getCurrentList()
        if (currentList != null) {
            userListDao.removeMovieFromList(currentList.listId, movieId)
        }
        // Update Movie Status
        movieDao.setMovieWatchlistStatus(movieId, inWatchlist = false)
    }

    override suspend fun removeMovieFromList(movieId: Int, listId: Long) = withContext(Dispatchers.IO) {
        userListDao.removeMovieFromList(listId, movieId)
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

    // --- List Management ---
    override suspend fun setCurrentList(listId: Long) = withContext(Dispatchers.IO) {
        userListDao.updateCurrentList(listId)
    }

    override suspend fun getCurrentList(): UserListEntity? = withContext(Dispatchers.IO) {
        userListDao.getCurrentList()
    }

    override suspend fun deleteUserList(listId: Long) = withContext(Dispatchers.IO) {
        userListDao.deleteListContents(listId)
        userListDao.deleteList(listId)
    }

    override suspend fun ensureDefaultListExists() = withContext(Dispatchers.IO) {
        val lists = userListDao.getAllLists()
        if (lists.isEmpty()) {
            userListDao.createList(UserListEntity(name = "My Watchlist", isCurrent = true))
        }
    }

    override suspend fun renameUserList(listId: Long, newName: String) = withContext(Dispatchers.IO) {
        userListDao.renameList(listId, newName)
    }

    // --- Ratings Impl ---
    override suspend fun setUserRating(movie: Movie, rating: Double) = withContext(Dispatchers.IO) {
        val existingEntity = movieDao.getMovieById(movie.id)
        val entityToInsert = if (existingEntity != null) {
            existingEntity.copy(userRating = rating, isInWatchlist = true)
        } else {
            movie.toEntity().copy(userRating = rating, isInWatchlist = true)
        }
        movieDao.insertOrUpdateMovie(entityToInsert)

        // Add to current list as well
        val currentList = userListDao.getCurrentList() ?: userListDao.getAllLists().firstOrNull()
        if (currentList != null) {
            val join = UserListMovieCrossRef(listId = currentList.listId, movieId = movie.id)
            userListDao.addMovieToList(join)
        }
    }

    override suspend fun getUserRatedMovies(): Result<List<Movie>> = withContext(Dispatchers.IO) {
        try {
            val entities = movieDao.getRatedMovies()
            Result.success(entities.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserRating(movieId: Int): Double = withContext(Dispatchers.IO) {
        movieDao.getMovieById(movieId)?.userRating ?: 0.0
    }

    // --- RECOMMENDATION ENGINE ---
    override suspend fun getPersonalizedRecommendations(): Result<List<Movie>> = withContext(Dispatchers.IO) {
        try {
            // 1. Gather Data
            val liked = movieDao.getLikedMovies()
            val rated = movieDao.getRatedMovies()
            val seedMovies = (liked + rated).distinctBy { it.id }

            // Log Data
            Log.d("RecEngine", "=== START GENERATION ===")
            Log.d("RecEngine", "Sources: ${liked.size} Liked, ${rated.size} Rated.")

            if (seedMovies.isEmpty()) {
                Log.d("RecEngine", "Profile empty.")
                return@withContext Result.success(emptyList())
            }

            // 2. Score Genres
            val genreCounts = mutableMapOf<Int, Int>()
            seedMovies.forEach { movie ->
                if (movie.genres.isNotBlank()) {
                    val ids = movie.genres.split(",").mapNotNull { it.toIntOrNull() }
                    ids.forEach { id ->
                        val weight = if (movie.isLiked) 3 else 1
                        val ratingWeight = if (movie.userRating > 0) movie.userRating.toInt() else 0
                        genreCounts[id] = genreCounts.getOrDefault(id, 0) + weight + ratingWeight
                    }
                }
            }

            // 3. Pick Top Genres
            val topGenres = genreCounts.entries.sortedByDescending { it.value }.take(2).map { it.key }
            Log.d("RecEngine", "Winner Genres: $topGenres")

            if (topGenres.isEmpty()) return@withContext Result.success(emptyList())

            val primaryGenre = topGenres[0]
            val secondaryGenre = topGenres.getOrNull(1)
            val recommendations = mutableListOf<Movie>()

            // 4. Query TMDB
            // Strategy A (Comfort Zone)
            val comfortResponse = apiService.discoverMovies(
                apiKey = BuildConfig.TMDB_API_KEY,
                genreId = primaryGenre.toString(),
                sortBy = "popularity.desc",
                voteAverageGte = 7.0,
                voteCountGte = 100
            )
            recommendations.addAll(comfortResponse.results.mapNotNull { it.toDomainModel() })

            // Strategy B (Mix)
            if (secondaryGenre != null) {
                val mixResponse = apiService.discoverMovies(
                    apiKey = BuildConfig.TMDB_API_KEY,
                    genreId = "$primaryGenre,$secondaryGenre",
                    sortBy = "vote_average.desc",
                    voteCountGte = 200
                )
                recommendations.addAll(mixResponse.results.mapNotNull { it.toDomainModel() })
            }

            // 5. Filter & Shuffle
            val seenIds = seedMovies.map { it.id }.toSet()
            val final = recommendations
                .filter { !seenIds.contains(it.id) }
                .distinctBy { it.id }
                .shuffled()
                .take(20)

            Log.d("RecEngine", "Final Result: ${final.size} movies.")
            Log.d("RecEngine", "=== END ===")

            Result.success(final)

        } catch (e: Exception) {
            Log.e("RecEngine", "Error: ${e.message}")
            Result.failure(e)
        }
    }
}

// --- MAPPERS ---

private fun Movie.toEntity(
    isInWatchlist: Boolean? = null,
    isLiked: Boolean? = null
): MovieEntity {
    return MovieEntity(
        id = this.id,
        title = this.title,
        posterPath = this.posterUrl,
        overview = this.overview,
        backdropPath = this.backdropUrl,
        releaseDate = this.releaseDate,
        voteAverage = this.rating,
        isInWatchlist = isInWatchlist ?: false,
        isLiked = isLiked ?: false,
        genres = this.genres.joinToString(","),
        userRating = this.userRating
    )
}

private fun MovieEntity.toDomainModel(): Movie {
    val posterBase = "https://image.tmdb.org/t/p/w500"
    val backdropBase = "https://image.tmdb.org/t/p/w780"

    val fixedPosterUrl = if (this.posterPath?.startsWith("http") == true) this.posterPath else "$posterBase${this.posterPath}"
    val fixedBackdropUrl = if (this.backdropPath?.startsWith("http") == true) this.backdropPath else "$backdropBase${this.backdropPath}"

    return Movie(
        id = this.id,
        title = this.title,
        posterUrl = fixedPosterUrl ?: "",
        backdropUrl = fixedBackdropUrl ?: "",
        overview = this.overview,
        releaseDate = this.releaseDate,
        rating = this.voteAverage,
        director = "",
        genres = if (this.genres.isNotEmpty()) this.genres.split(",").mapNotNull { it.toIntOrNull() } else emptyList(),
        userRating = this.userRating
    )
}

// --- NEW: DTO MAPPER FOR RECOMMENDATIONS ---
// This is required because 'getPersonalizedRecommendations' gets raw DTOs from API
// and needs to convert them to Domain Movies to return.
private fun MovieDto.toDomainModel(): Movie? {
    if (this.posterPath == null) return null
    val posterBase = "https://image.tmdb.org/t/p/w500"
    val backdropBase = "https://image.tmdb.org/t/p/w780"

    return Movie(
        id = this.id,
        title = this.title,
        posterUrl = "$posterBase${this.posterPath}",
        backdropUrl = if (this.backdropPath != null) "$backdropBase${this.backdropPath}" else "$posterBase${this.posterPath}",
        overview = this.overview ?: "",
        releaseDate = this.releaseDate ?: "",
        rating = this.voteAverage ?: 0.0,
        genres = this.genreIds ?: emptyList()
    )
}