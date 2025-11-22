package com.example.cinephile.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.domain.repository.MovieRepository
import com.example.cinephile.domain.repository.UserCollectionsRepository // <--- Import this
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val movies: List<Movie>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

// UPDATE CONSTRUCTOR: Add userRepo
class HomeViewModel(
    private val movieRepo: MovieRepository,
    private val userRepo: UserCollectionsRepository
) : ViewModel() {

    private val _trendingState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val trendingState: StateFlow<HomeUiState> = _trendingState

    private val _recommendationState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val recommendationState: StateFlow<HomeUiState> = _recommendationState

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            // 1. Get Trending
            movieRepo.getTrendingMovies()
                .onSuccess { movies -> _trendingState.value = HomeUiState.Success(movies) }
                .onFailure { e -> _trendingState.value = HomeUiState.Error(e.message ?: "Error") }

            // 2. Get Recommendations
            movieRepo.getRecommendedMovies(userId = 0)
                .onSuccess { movies -> _recommendationState.value = HomeUiState.Success(movies) }
                .onFailure { _recommendationState.value = HomeUiState.Error("Failed to load") }
        }
    }

    // --- NEW FUNCTION: Save to Watchlist ---
    fun addToWatchlist(movie: Movie) {
        viewModelScope.launch {
            userRepo.addMovieToWatchlist(movie)
        }
    }
}