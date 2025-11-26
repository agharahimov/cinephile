package com.example.cinephile.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cinephile.data.MovieRepositoryImpl
import com.example.cinephile.data.QuizRepositoryImpl
import com.example.cinephile.data.UserCollectionsRepositoryImpl
import com.example.cinephile.data.UserRepositoryImpl
import com.example.cinephile.data.local.AppDatabase
import com.example.cinephile.ui.auth.AuthViewModel
import com.example.cinephile.ui.details.DetailsViewModel
import com.example.cinephile.ui.favorites.FavoritesViewModel
import com.example.cinephile.ui.home.HomeViewModel
import com.example.cinephile.ui.quiz.QuizViewModel
import com.example.cinephile.ui.search.SearchViewModel
import com.example.cinephile.ui.watchlist.WatchlistViewModel
import com.example.cinephile.ui.watchlist.WatchlistManagerViewModel // <--- THIS WAS MISSING

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Get Database Instance
        val db = AppDatabase.getDatabase(context)

        return when {
            // 1. Auth (Login/Signup)
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                val repository = UserRepositoryImpl(db.userDao())
                AuthViewModel(repository) as T
            }

            // 2. Search
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                val repository = MovieRepositoryImpl()
                // Pass DB repo too if you updated SearchViewModel to support adding to watchlist
                val dbRepo = UserCollectionsRepositoryImpl(db.movieDao(), db.userListDao())
                SearchViewModel(repository, dbRepo) as T
            }

            // 3. Home (Trending)
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                val apiRepo = MovieRepositoryImpl()
                val dbRepo = UserCollectionsRepositoryImpl(db.movieDao(), db.userListDao())
                HomeViewModel(apiRepo, dbRepo) as T
            }

            // 4. Details Screen
            modelClass.isAssignableFrom(DetailsViewModel::class.java) -> {
                val apiRepo = MovieRepositoryImpl()
                val dbRepo = UserCollectionsRepositoryImpl(db.movieDao(), db.userListDao())
                DetailsViewModel(apiRepo, dbRepo) as T
            }

            // 5. Favorites
            modelClass.isAssignableFrom(FavoritesViewModel::class.java) -> {
                val repository = UserCollectionsRepositoryImpl(db.movieDao(), db.userListDao())
                FavoritesViewModel(repository) as T
            }

            // 6. Watchlist Details (The Grid of Movies)
            modelClass.isAssignableFrom(WatchlistViewModel::class.java) -> {
                val repository = UserCollectionsRepositoryImpl(db.movieDao(), db.userListDao())
                WatchlistViewModel(repository) as T
            }

            // 7. Watchlist Manager (The List of Lists)
            modelClass.isAssignableFrom(WatchlistManagerViewModel::class.java) -> {
                val repository = UserCollectionsRepositoryImpl(db.movieDao(), db.userListDao())
                WatchlistManagerViewModel(repository) as T
            }

            // 8. quiz
            modelClass.isAssignableFrom(QuizViewModel::class.java) -> {
                val quizRepo = QuizRepositoryImpl(db.movieDao(), db.userListDao())
                QuizViewModel(quizRepo) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}