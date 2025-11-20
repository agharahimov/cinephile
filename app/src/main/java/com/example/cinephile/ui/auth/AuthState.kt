package com.example.cinephile.ui.auth

// A sealed class is perfect for representing a finite number of states
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val userId: Int) : AuthState()
    data class Error(val message: String) : AuthState()
}