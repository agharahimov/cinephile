package com.example.cinephile.ui.home

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cinephile.R
import com.example.cinephile.ui.ViewModelFactory
import com.example.cinephile.ui.search.MovieAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var viewModel: HomeViewModel
    private lateinit var trendingAdapter: MovieAdapter
    private lateinit var recommendationAdapter: MovieAdapter

    // --- FIX: Declare UI variables here so they are seen by the whole class ---
    private lateinit var rvTrending: RecyclerView
    private lateinit var rvRecommendations: RecyclerView
    private lateinit var pbTrending: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- FIX: Initialize them using findViewById ---
        rvTrending = view.findViewById(R.id.rvTrending)
        rvRecommendations = view.findViewById(R.id.rvRecommendations)
        pbTrending = view.findViewById(R.id.progressBar) // Note: In XML it's likely @+id/progressBar

        // 1. Setup ViewModel
        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        // 2. Setup Trending List (Horizontal)
        trendingAdapter = MovieAdapter(
            onMovieClick = { movie ->
                Toast.makeText(context, movie.title, Toast.LENGTH_SHORT).show()
            },
            onMovieLongClick = { }
        )

        // Now 'rvTrending' is recognized!
        rvTrending.apply {
            adapter = trendingAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // 3. Setup Recommendation List (Horizontal)
        recommendationAdapter = MovieAdapter(
            onMovieClick = { movie ->
                Toast.makeText(context, movie.title, Toast.LENGTH_SHORT).show()
            },
            onMovieLongClick = { }
        )

        // Now 'rvRecommendations' is recognized!
        rvRecommendations.apply {
            adapter = recommendationAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // 4. Observe Data
        lifecycleScope.launch {
            viewModel.trendingState.collect { state ->
                if (state is HomeUiState.Success) {
                    trendingAdapter.submitList(state.movies)
                    // Now 'pbTrending' is recognized!
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
}