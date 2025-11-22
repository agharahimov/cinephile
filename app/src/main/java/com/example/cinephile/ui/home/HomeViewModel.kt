package com.example.cinephile.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.domain.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val movies: List<Movie>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(private val repository: MovieRepository) : ViewModel() {

    // State for Trending List
    private val _trendingState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val trendingState: StateFlow<HomeUiState> = _trendingState

    // State for Recommendations (Placeholder)
    private val _recommendationState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val recommendationState: StateFlow<HomeUiState> = _recommendationState

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            // 1. Fetch Trending
            repository.getTrendingMovies()
                .onSuccess { movies -> _trendingState.value = HomeUiState.Success(movies) }
                .onFailure { e -> _trendingState.value = HomeUiState.Error(e.message ?: "Error") }

            // 2. Fetch Recommendations (Currently fetches Top Rated as placeholder)
            repository.getRecommendedMovies(userId = 0) // User ID 0 for now
                .onSuccess { movies -> _recommendationState.value = HomeUiState.Success(movies) }
                .onFailure { _recommendationState.value = HomeUiState.Error("Failed to load") }
        }
    }
}