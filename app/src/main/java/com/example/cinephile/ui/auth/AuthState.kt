package com.example.cinephile.ui.auth

import com.example.cinephile.data.local.UserEntity

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class LoginSuccess(val user: UserEntity) : AuthState()
    data class Error(val message: String) : AuthState()
    object RegistrationSuccess : AuthState()
}