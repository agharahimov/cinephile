package com.example.cinephile.ui.home

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cinephile.R
import com.example.cinephile.ui.ViewModelFactory
import com.example.cinephile.ui.search.MovieAdapter
import com.example.cinephile.util.GuestManager
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var viewModel: HomeViewModel
    private lateinit var trendingAdapter: MovieAdapter
    private lateinit var recommendationAdapter: MovieAdapter

    // --- 1. DEFINE UI VARIABLES (Class Level) ---
    private lateinit var rvTrending: RecyclerView
    private lateinit var rvRecommendations: RecyclerView
    private lateinit var pbTrending: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 2. INITIALIZE VIEWS ---
        rvTrending = view.findViewById(R.id.rvTrending)
        rvRecommendations = view.findViewById(R.id.rvRecommendations)
        pbTrending = view.findViewById(R.id.progressBar) // Matches XML id: progressBar

        val tvRecTitle = view.findViewById<TextView>(R.id.tvRecTitle)
        val tvRecPlaceholder = view.findViewById<TextView>(R.id.tvRecPlaceholder)

        // --- NEW GUEST VIEWS ---
        val layoutGuestMode = view.findViewById<View>(R.id.layoutGuestMode)
        val btnGuestLogin = view.findViewById<View>(R.id.btnGuestLogin)

        // 3. Init ViewModel
        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        // ==============================================================
        // 4. CLICK LISTENER (The Gatekeeper)
        // ==============================================================
        // Allows Guests to click "Recommended", but blocks navigation with a dialog
        tvRecTitle.setOnClickListener {
            GuestManager.checkAndRun(requireContext(), findNavController()) {
                findNavController().navigate(R.id.action_homeFragment_to_recommendationFragment)
            }
        }

        // ==============================================================
        // 5. SETUP TRENDING (Visible to Everyone)
        // ==============================================================
        trendingAdapter = MovieAdapter(
            onMovieClick = { movie ->
                openMovieDetails(movie.id)
            },
            onMovieLongClick = { movie ->
                // Block Guest from saving to watchlist
                GuestManager.checkAndRun(requireContext(), findNavController()) {
                    viewModel.addToWatchlist(movie)
                    Toast.makeText(requireContext(), "${movie.title} added", Toast.LENGTH_SHORT).show()
                }
            }
        )

        rvTrending.apply {
            adapter = trendingAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // Observe Trending Data
        lifecycleScope.launch {
            viewModel.trendingState.collect { state ->
                if (state is HomeUiState.Success) {
                    trendingAdapter.submitList(state.movies)
                    pbTrending.visibility = View.GONE
                }
            }
        }

        // ==============================================================
        // 6. SETUP RECOMMENDATIONS (Guest Logic)
        // ==============================================================
        val isGuest = GuestManager.isGuest(requireContext())

        if (isGuest) {
            // --- GUEST MODE ---
            // Hide the list and placeholder
            rvRecommendations.visibility = View.GONE
            tvRecPlaceholder.visibility = View.GONE

            // Keep Title Visible (so they can click it and see the dialog)
            tvRecTitle.visibility = View.VISIBLE

            // Show the nice Banner
            layoutGuestMode.visibility = View.VISIBLE

            // Handle Login Button Click
            btnGuestLogin.setOnClickListener {
                findNavController().navigate(R.id.loginFragment)
            }

        } else {
            // --- USER MODE ---
            layoutGuestMode.visibility = View.GONE // Hide Banner
            tvRecTitle.visibility = View.VISIBLE
            rvRecommendations.visibility = View.VISIBLE

            recommendationAdapter = MovieAdapter(
                onMovieClick = { movie -> openMovieDetails(movie.id) },
                onMovieLongClick = { movie ->
                    viewModel.addToWatchlist(movie)
                    Toast.makeText(requireContext(), "${movie.title} added", Toast.LENGTH_SHORT).show()
                }
            )

            rvRecommendations.apply {
                adapter = recommendationAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }

            // Observe Recommendations Data
            lifecycleScope.launch {
                viewModel.recommendationState.collect { state ->
                    when (state) {
                        is HomeUiState.Success -> {
                            recommendationAdapter.submitList(state.movies)
                            tvRecPlaceholder.visibility = View.GONE
                        }
                        is HomeUiState.Error -> {
                            tvRecPlaceholder.visibility = View.VISIBLE
                            tvRecPlaceholder.text = state.message
                        }
                        is HomeUiState.Loading -> { }
                    }
                }
            }
        }
    }

    private fun openMovieDetails(movieId: Int) {
        val bundle = Bundle().apply { putInt("movieId", movieId) }
        findNavController().navigate(R.id.action_global_detailsFragment, bundle)
    }
}