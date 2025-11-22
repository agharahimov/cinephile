package com.example.cinephile.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    fun toggleFavorite() {
        val currentMovie = _uiState.value.movie ?: return
        viewModelScope.launch {
            if (_uiState.value.isFavorite) {
                userRepo.unlikeMovie(currentMovie.id)
            } else {
                userRepo.likeMovie(currentMovie)
            }
            // Update UI immediately
            _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite)
        }
    }

    // Toggle Watchlist logic
    fun toggleWatchlist() {
        val currentMovie = _uiState.value.movie ?: return
        viewModelScope.launch {
            if (_uiState.value.isInWatchlist) {
                userRepo.removeMovieFromWatchlist(currentMovie.id)
            } else {
                userRepo.addMovieToWatchlist(currentMovie)
            }
            // Update UI immediately
            _uiState.value = _uiState.value.copy(isInWatchlist = !_uiState.value.isInWatchlist)
        }
    }
}