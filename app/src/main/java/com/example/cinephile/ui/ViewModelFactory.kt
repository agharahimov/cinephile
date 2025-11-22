package com.example.cinephile.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cinephile.data.MovieRepositoryImpl
import com.example.cinephile.data.UserCollectionsRepositoryImpl
import com.example.cinephile.data.UserRepositoryImpl
import com.example.cinephile.data.local.AppDatabase
import com.example.cinephile.ui.auth.AuthViewModel
import com.example.cinephile.ui.search.SearchViewModel
import com.example.cinephile.ui.watchlist.WatchlistViewModel // This will be created in Step 2

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Get Database Instance
        val db = AppDatabase.getDatabase(context)

        return when {
            // 1. Auth
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                val repository = UserRepositoryImpl(db.userDao())
                AuthViewModel(repository) as T
            }
            // 2. Search
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                val repository = MovieRepositoryImpl()
                SearchViewModel(repository) as T
            }
            // 3. Watchlist (NEW ADDITION)
            modelClass.isAssignableFrom(WatchlistViewModel::class.java) -> {
                // Inject the specific DAOs required by UserCollectionsRepositoryImpl
                val repository = UserCollectionsRepositoryImpl(db.movieDao(), db.userListDao())
                WatchlistViewModel(repository) as T
            }
            // 4. Home (Trending/Recos)
            modelClass.isAssignableFrom(com.example.cinephile.ui.home.HomeViewModel::class.java) -> {
                val repository = MovieRepositoryImpl()
                com.example.cinephile.ui.home.HomeViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}