package com.example.cinephile.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cinephile.R
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.ui.ViewModelFactory
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

        rvTrending = view.findViewById(R.id.rvTrending)
        rvRecommendations = view.findViewById(R.id.rvRecommendations)
        pbTrending = view.findViewById(R.id.progressBar)

        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        // --- UPDATED ADAPTER LOGIC ---

        // Trending List
        trendingAdapter = MovieAdapter(
            onMovieClick = { movie -> openMovieDetails(movie.id) },
            onMovieLongClick = { movie -> showAddToListDialog(movie) } // Show Dialog
        )

        rvTrending.apply {
            adapter = trendingAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // Recommendation List
        recommendationAdapter = MovieAdapter(
            onMovieClick = { movie -> openMovieDetails(movie.id) },
            onMovieLongClick = { movie -> showAddToListDialog(movie) } // Show Dialog
        )

        rvRecommendations.apply {
            adapter = recommendationAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // (Rest of logic remains the same)
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

    private fun openMovieDetails(movieId: Int) {
        val bundle = Bundle().apply { putInt("movieId", movieId) }
        findNavController().navigate(R.id.action_global_detailsFragment, bundle)
    }

    // --- NEW HELPER FUNCTION ---
    private fun showAddToListDialog(movie: Movie) {
        viewModel.getUserLists { lists ->
            if (lists.isEmpty()) {
                viewModel.addToWatchlist(movie)
                Toast.makeText(context, "Added to default Watchlist", Toast.LENGTH_SHORT).show()
                return@getUserLists
            }
            // Case 2: Single List -> Direct Add
            if (lists.size == 1) {
                val list = lists[0]
                viewModel.addMovieToSpecificList(movie, list.listId)
                Toast.makeText(context, "Added to ${list.name}", Toast.LENGTH_SHORT).show()
                return@getUserLists
            }

            // Case 3: Multiple Lists -> Show Dialog
            val listNames = lists.map { it.name }.toTypedArray()

            AlertDialog.Builder(requireContext())
                .setTitle("Add to which list?")
                .setItems(listNames) { _, which ->
                    val selectedList = lists[which]
                    viewModel.addMovieToSpecificList(movie, selectedList.listId)
                    Toast.makeText(context, "Added to ${selectedList.name}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}