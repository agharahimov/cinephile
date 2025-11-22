package com.example.cinephile.ui.home

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController // <--- IMPORT THIS
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cinephile.R
import com.example.cinephile.ui.ViewModelFactory
import com.example.cinephile.ui.home.HomeUiState
import com.example.cinephile.ui.home.HomeViewModel
import com.example.cinephile.ui.search.MovieAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var viewModel: HomeViewModel
    private lateinit var trendingAdapter: MovieAdapter
    private lateinit var recommendationAdapter: MovieAdapter

    private lateinit var rvTrending: RecyclerView
    private lateinit var rvRecommendations: RecyclerView
    private lateinit var pbTrending: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Variables
        rvTrending = view.findViewById(R.id.rvTrending)
        rvRecommendations = view.findViewById(R.id.rvRecommendations)
        pbTrending = view.findViewById(R.id.progressBar)

        // 2. ViewModel
        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        // ==============================================================
        // 3. SETUP ADAPTERS (UPDATED CLICK LOGIC)
        // ==============================================================

        // --- TRENDING LIST ---
        trendingAdapter = MovieAdapter(
            onMovieClick = { movie ->
                openMovieDetails(movie.id)
            },
            onMovieLongClick = { movie ->
                // 1. Save to Database
                viewModel.addToWatchlist(movie)

                // 2. Show Confirmation
                Toast.makeText(requireContext(), "${movie.title} added to Watchlist", Toast.LENGTH_SHORT).show()
            }
        )

        rvTrending.apply {
            adapter = trendingAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // --- RECOMMENDATION LIST ---
        recommendationAdapter = MovieAdapter(
            onMovieClick = { movie ->
                openMovieDetails(movie.id)
            },
            onMovieLongClick = { movie ->
                // Same logic for recommendations
                viewModel.addToWatchlist(movie)
                Toast.makeText(requireContext(), "${movie.title} added to Watchlist", Toast.LENGTH_SHORT).show()
            }
        )

        rvRecommendations.apply {
            adapter = recommendationAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // 4. Observe Data (Same as before)
        lifecycleScope.launch {
            viewModel.trendingState.collect { state ->
                if (state is HomeUiState.Success) {
                    trendingAdapter.submitList(state.movies)
                    pbTrending.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.recommendationState.collect { state ->
                if (state is HomeUiState.Success) {
                    recommendationAdapter.submitList(state.movies)
                }
            }
        }
    }

    // --- HELPER FUNCTION TO NAVIGATE ---
    private fun openMovieDetails(movieId: Int) {
        val bundle = Bundle().apply {
            putInt("movieId", movieId)
        }
        // Navigate using the Global Action we defined in nav_graph.xml
        findNavController().navigate(R.id.action_global_detailsFragment, bundle)
    }
}