package com.example.cinephile.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinephile.domain.repository.UserCollectionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecommendationViewModel(
    private val repository: UserCollectionsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadRecommendations()
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            // Call our new algorithm
            val result = repository.getPersonalizedRecommendations()

            result.onSuccess { movies ->
                if (movies.isEmpty()) {
                    // Provide a clear message if we can't generate recommendations yet
                    _uiState.value = HomeUiState.Error("Rate or Like more movies to get recommendations!")
                } else {
                    _uiState.value = HomeUiState.Success(movies)
                }
            }.onFailure { e ->
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to load")
            }
        }
    }
}