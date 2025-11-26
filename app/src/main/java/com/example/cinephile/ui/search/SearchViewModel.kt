package com.example.cinephile.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinephile.data.local.UserListEntity
import com.example.cinephile.domain.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.cinephile.domain.repository.UserCollectionsRepository
import com.example.cinephile.domain.model.Movie

class   SearchViewModel(
    private val movieRepository: MovieRepository,
    private val userRepo: UserCollectionsRepository
    ) : ViewModel() {


    // CORRECTED: The StateFlow now correctly holds a SearchUiState object.
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState

    private var searchJob: Job? = null

    fun searchMovies(query: String, searchType: SearchType = SearchType.TITLE) {
        // Cancel any previous search job
        searchJob?.cancel()

        // Business rule: If the query is too short, just show an empty list and stop.
        if (query.length < 2) {
            _uiState.value = SearchUiState.Success(emptyList())
            return
        }

        searchJob = viewModelScope.launch {
            // Debounce: Wait for 500ms after the user stops typing.
            delay(500)

            _uiState.value = SearchUiState.Loading

            val result = movieRepository.searchMovies(query, searchType)

            result.onSuccess { movies ->
                _uiState.value = SearchUiState.Success(movies)
            }.onFailure { error ->
                _uiState.value = SearchUiState.Error(error.message ?: "An unknown error occurred")
            }
        }
    }

    //list logic
    // 1. Get all custom lists to show in the Dialog
    fun getUserLists(onResult: (List<UserListEntity>) -> Unit) {
        viewModelScope.launch {
            // We assume getAllCustomLists returns Result<List<UserListEntity>>
            val result = userRepo.getAllCustomLists()
            result.onSuccess { lists ->
                onResult(lists)
            }.onFailure {
                onResult(emptyList())
            }
        }
    }

    // 2. Add to a specific list ID
    fun addMovieToSpecificList(movie: Movie, listId: Long) {
        viewModelScope.launch {
            // 1. Add to database (default watchlist logic)
            userRepo.addMovieToWatchlist(movie)
            // 2. Link to the specific custom list
            userRepo.addMovieToCustomList(movie.id, listId)
        }
    }
}
