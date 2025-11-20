package com.example.cinephile.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinephile.domain.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Please fill all fields")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            // FIX: Handle the Result wrapper
            val result = repository.login(email, pass)

            result.onSuccess { user ->
                _authState.value = AuthState.LoginSuccess(user)
            }
            result.onFailure {
                _authState.value = AuthState.Error("Invalid Credentials")
            }
        }
    }

    fun register(username: String, email: String, pass: String) {
        if (username.isBlank() || email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Please fill all fields")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // FIX: Pass all 3 arguments (username, email, pass)
            // FIX: Handle the Result wrapper
            val result = repository.register(username, email, pass)

            result.onSuccess {
                _authState.value = AuthState.RegistrationSuccess
            }
            result.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Registration Failed")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}