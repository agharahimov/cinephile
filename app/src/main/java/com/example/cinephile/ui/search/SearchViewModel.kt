package com.example.cinephile.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.domain.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<List<Movie>>(emptyList())
    val uiState: StateFlow<List<Movie>> = _uiState

    fun searchMovies(query: String) {
        viewModelScope.launch {
            val result = repository.searchMovies(query)
            result.onSuccess { movies ->
                _uiState.value = movies
            }
            // TODO: Handle result.onFailure in a future task
        }
    }
}