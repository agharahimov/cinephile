package com.example.cinephile.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinephile.domain.repository.UserCollectionsRepository
import com.example.cinephile.ui.watchlist.WatchlistUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: UserCollectionsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<WatchlistUiState>(WatchlistUiState.Loading)
    val uiState: StateFlow<WatchlistUiState> = _uiState

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = WatchlistUiState.Loading
            val result = repository.getLikedMovies() // <--- CALLING GET LIKED MOVIES

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
}