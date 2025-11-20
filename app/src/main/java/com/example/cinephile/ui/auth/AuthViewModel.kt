package com.example.cinephile.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinephile.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = userRepository.login(email, password)
            result.onSuccess { userId ->
                _authState.value = AuthState.Authenticated(userId)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "An unknown error occurred")
            }
        }
    }

    // TODO: Add a register function here as well
}