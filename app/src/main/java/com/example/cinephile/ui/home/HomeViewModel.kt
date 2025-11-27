package com.example.cinephile.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinephile.data.local.UserListEntity
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
            // 1. Get Trending (Keep this as is - it's always good content)
            movieRepo.getTrendingMovies()
                .onSuccess { movies -> _trendingState.value = HomeUiState.Success(movies) }
                .onFailure { e -> _trendingState.value = HomeUiState.Error(e.message ?: "Error") }

            // 2. Get Recommendations (SMART LOGIC)
            // First, try to get personalized suggestions
            val personalResult = userRepo.getPersonalizedRecommendations()

            personalResult.onSuccess { movies ->
                if (movies.isNotEmpty()) {
                    // Success! We have personalized data.
                    _recommendationState.value = HomeUiState.Success(movies)
                } else {
                    // STOP: Do not load generic movies. Tell user what to do.
                    _recommendationState.value = HomeUiState.Error("Rate or Like movies to see recommendations here.")
                }
            }.onFailure {
                _recommendationState.value = HomeUiState.Error("Rate or Like movies to see recommendations here.")
            }
        }
    }

    fun getUserLists(onResult: (List<UserListEntity>) -> Unit) {
        viewModelScope.launch {
            userRepo.getAllCustomLists().onSuccess { onResult(it) }
        }
    }

    fun addToWatchlist(movie: Movie) {
        viewModelScope.launch {
            userRepo.addMovieToWatchlist(movie)
        }
    }

    fun addMovieToSpecificList(movie: Movie, listId: Long) {
        viewModelScope.launch {
            userRepo.addMovieToWatchlist(movie)
            userRepo.addMovieToCustomList(movie.id, listId)
        }
    }

    private suspend fun loadGenericRecommendations() {
        movieRepo.getRecommendedMovies(userId = 0)
            .onSuccess { movies ->
                _recommendationState.value = HomeUiState.Success(movies)
            }
            .onFailure {
                _recommendationState.value = HomeUiState.Error("Failed to load")
            }
    }
}