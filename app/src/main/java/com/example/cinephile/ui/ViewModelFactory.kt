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
                val apiRepo = MovieRepositoryImpl()
                // Create DB Repo
                val dbRepo = UserCollectionsRepositoryImpl(db.movieDao(), db.userListDao())

                // Pass BOTH to SearchViewModel
                SearchViewModel(apiRepo, dbRepo) as T
            }
            // 3. Watchlist (NEW ADDITION)
            modelClass.isAssignableFrom(WatchlistViewModel::class.java) -> {
                // Inject the specific DAOs required by UserCollectionsRepositoryImpl
                val repository = UserCollectionsRepositoryImpl(db.movieDao(), db.userListDao())
                WatchlistViewModel(repository) as T
            }
            // 4. Home (Trending/Recos)
            modelClass.isAssignableFrom(com.example.cinephile.ui.home.HomeViewModel::class.java) -> {
                val apiRepo = MovieRepositoryImpl()
                // Create the Database Repo
                val dbRepo = UserCollectionsRepositoryImpl(db.movieDao(), db.userListDao())

                // Pass BOTH to HomeViewModel
                com.example.cinephile.ui.home.HomeViewModel(apiRepo, dbRepo) as T
            }
            // 5. Details Screen (Requires API + DB)
            modelClass.isAssignableFrom(com.example.cinephile.ui.details.DetailsViewModel::class.java) -> {
                val apiRepo = MovieRepositoryImpl()
                // Use the DB to create the UserRepo
                val dbRepo = UserCollectionsRepositoryImpl(db.movieDao(), db.userListDao())

                com.example.cinephile.ui.details.DetailsViewModel(apiRepo, dbRepo) as T
            }
            // 7. Favorites
            modelClass.isAssignableFrom(com.example.cinephile.ui.favorites.FavoritesViewModel::class.java) -> {
                val dbRepo = UserCollectionsRepositoryImpl(db.movieDao(), db.userListDao())
                com.example.cinephile.ui.favorites.FavoritesViewModel(dbRepo) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}