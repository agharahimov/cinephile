package com.example.cinephile.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.domain.repository.UserCollectionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Define the states for the UI
sealed class WatchlistUiState {
    object Loading : WatchlistUiState()
    object Empty : WatchlistUiState()
    data class Success(val movies: List<Movie>) : WatchlistUiState()
}

class WatchlistViewModel(private val repository: UserCollectionsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<WatchlistUiState>(WatchlistUiState.Loading)
    val uiState: StateFlow<WatchlistUiState> = _uiState

    // Load data as soon as ViewModel is created
    init {
        loadWatchlist()
    }

    fun loadWatchlist() {
        viewModelScope.launch {
            _uiState.value = WatchlistUiState.Loading
            val result = repository.getWatchlist()

            result.onSuccess { movies ->
                if (movies.isEmpty()) {
                    _uiState.value = WatchlistUiState.Empty
                } else {
                    _uiState.value = WatchlistUiState.Success(movies)
                }
            }.onFailure {
                _uiState.value = WatchlistUiState.Empty
            }
        }
    }

    fun removeFromWatchlist(movie: Movie) {
        viewModelScope.launch {
            repository.removeMovieFromWatchlist(movie.id)
            loadWatchlist() // Refresh the list
        }
    }
}