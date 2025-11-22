package com.example.cinephile.ui.favorites

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cinephile.R
import com.example.cinephile.ui.ViewModelFactory
import com.example.cinephile.ui.search.MovieAdapter
import com.example.cinephile.ui.watchlist.WatchlistUiState
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment(R.layout.fragment_watchlist) { // Reusing Watchlist Layout

    private lateinit var viewModel: FavoritesViewModel
    private lateinit var movieAdapter: MovieAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Change Header Text since we are reusing layout
        view.findViewById<TextView>(R.id.tvHeader).text = "Your Favorites"

        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[FavoritesViewModel::class.java]

        val rvList = view.findViewById<RecyclerView>(R.id.rvWatchlist)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layoutEmptyState)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        movieAdapter = MovieAdapter(
            onMovieClick = { movie ->
                val bundle = Bundle().apply { putInt("movieId", movie.id) }
                findNavController().navigate(R.id.action_global_detailsFragment, bundle)
            },
            onMovieLongClick = { }
        )

        rvList.layoutManager = GridLayoutManager(context, 3)
        rvList.adapter = movieAdapter

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is WatchlistUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        rvList.visibility = View.GONE
                        layoutEmpty.visibility = View.GONE
                    }
                    is WatchlistUiState.Empty -> {
                        progressBar.visibility = View.GONE
                        rvList.visibility = View.GONE
                        layoutEmpty.visibility = View.VISIBLE
                        // Optional: Update empty text
                        view.findViewById<TextView>(R.id.tvRecPlaceholder)?.text = "No favorites yet"
                    }
                    is WatchlistUiState.Success -> {
                        progressBar.visibility = View.GONE
                        layoutEmpty.visibility = View.GONE
                        rvList.visibility = View.VISIBLE
                        movieAdapter.submitList(state.movies)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites()
    }
}