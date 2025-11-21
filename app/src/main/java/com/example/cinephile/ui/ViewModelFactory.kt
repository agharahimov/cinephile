package com.example.cinephile.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cinephile.data.MovieRepositoryImpl
import com.example.cinephile.data.UserRepositoryImpl
import com.example.cinephile.data.local.AppDatabase
import com.example.cinephile.ui.auth.AuthViewModel
import com.example.cinephile.ui.search.SearchViewModel

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                // This line should now resolve correctly
                val db = AppDatabase.getDatabase(context)
                val repository = UserRepositoryImpl(db.userDao())
                AuthViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                val repository = MovieRepositoryImpl()
                SearchViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}