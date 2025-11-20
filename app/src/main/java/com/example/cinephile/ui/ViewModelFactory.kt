package com.example.cinephile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cinephile.data.UserRepositoryImpl
import com.example.cinephile.data.local.UserDao
import com.example.cinephile.ui.auth.AuthViewModel

// This is a simplified factory. A real app would use a dependency injection
// framework like Hilt or Koin to manage this.
class ViewModelFactory(private val userDao: UserDao) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            // In a real app, you would get the repository from a dependency graph
            val repository = UserRepositoryImpl(userDao)
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}