package com.example.cinephile.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinephile.data.local.UserListEntity
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.domain.repository.MovieRepository
import com.example.cinephile.domain.repository.UserCollectionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// The UI State: Holds everything the screen needs to draw
data class DetailsUiState(
    val movie: Movie? = null,
    val isLoading: Boolean = false,
    val isFavorite: Boolean = false,
    val isInWatchlist: Boolean = false,
    val error: String? = null
)

class DetailsViewModel(
    private val movieRepo: MovieRepository,
    private val userRepo: UserCollectionsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState(isLoading = true))
    val uiState: StateFlow<DetailsUiState> = _uiState

    // Called when screen opens
    fun loadMovie(movieId: Int) {
        viewModelScope.launch {
            // 1. Check Database (Is it already liked/watchlisted?)
            val isFav = userRepo.isMovieLiked(movieId)
            val isWatch = userRepo.isMovieInWatchlist(movieId)

            _uiState.value = _uiState.value.copy(isFavorite = isFav, isInWatchlist = isWatch)

            // 2. Fetch Full Details from API
            movieRepo.getMovieDetails(movieId)
                .onSuccess { movie ->
                    _uiState.value = _uiState.value.copy(movie = movie, isLoading = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(error = "Failed to load details", isLoading = false)
                }
        }
    }

    // Toggle Favorite logic
    // --- TOGGLE FAVORITE ---
    fun toggleFavorite(movie: Movie? = _uiState.value.movie) {
        val targetMovie = movie ?: return

        viewModelScope.launch {
            if (userRepo.isMovieLiked(targetMovie.id)) {
                userRepo.unlikeMovie(targetMovie.id)
                // Update State: It was true, now it's false
                _uiState.value = _uiState.value.copy(isFavorite = false)
            } else {
                userRepo.likeMovie(targetMovie)
                // Update State: It was false, now it's true
                _uiState.value = _uiState.value.copy(isFavorite = true)
            }
        }
    }
    fun checkDatabaseStatus(movieId: Int) {
        viewModelScope.launch {
            val isFav = userRepo.isMovieLiked(movieId)
            val isWatch = userRepo.isMovieInWatchlist(movieId)
            // Update UI State instantly
            _uiState.value = _uiState.value.copy(isFavorite = isFav, isInWatchlist = isWatch)
        }
    }

    // Toggle Watchlist logic
    fun toggleWatchlist(movie: Movie? = _uiState.value.movie) {
        val targetMovie = movie ?: return

        viewModelScope.launch {
            // 1. Database Operation
            if (userRepo.isMovieInWatchlist(targetMovie.id)) {
                userRepo.removeMovieFromWatchlist(targetMovie.id)
            } else {
                userRepo.addMovieToWatchlist(targetMovie)
            }

            // 2. ALWAYS update the UI state
            // Removed: if (_uiState.value.movie?.id == targetMovie.id)
            _uiState.value = _uiState.value.copy(isInWatchlist = !_uiState.value.isInWatchlist)
        }
    }

    fun getUserLists(onResult: (List<UserListEntity>) -> Unit) {
        viewModelScope.launch {
            userRepo.getAllCustomLists().onSuccess { onResult(it) }
        }
    }

    fun addMovieToSpecificList(movie: Movie, listId: Long) {
        viewModelScope.launch {
            userRepo.addMovieToWatchlist(movie)
            userRepo.addMovieToCustomList(movie.id, listId)
            checkDatabaseStatus(movie.id) // Refresh UI
        }
    }

    fun rateMovie(movie: Movie) {
        viewModelScope.launch {
            userRepo.setUserRating(movie, movie.userRating)
            // Refresh UI state
            checkDatabaseStatus(movie.id)
        }
    }
}