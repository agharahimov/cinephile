package com.example.cinephile.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.domain.repository.UserCollectionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class WatchlistUiState {
    object Loading : WatchlistUiState()
    object Empty : WatchlistUiState()
    data class Success(val movies: List<Movie>) : WatchlistUiState()
}

class WatchlistViewModel(private val repository: UserCollectionsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<WatchlistUiState>(WatchlistUiState.Loading)
    val uiState: StateFlow<WatchlistUiState> = _uiState

    // Load movies for a specific Custom List
    fun loadCustomList(listId: Long) {
        viewModelScope.launch {
            _uiState.value = WatchlistUiState.Loading

            // If ID is 0, load legacy/default list. If ID > 0, load custom list.
            val result = if (listId == 0L) repository.getWatchlist() else repository.getMoviesInCustomList(listId)

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
        }
    }
}