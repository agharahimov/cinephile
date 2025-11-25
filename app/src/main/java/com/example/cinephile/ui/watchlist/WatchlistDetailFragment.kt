package com.example.cinephile.ui.watchlist

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cinephile.R
import com.example.cinephile.ui.ViewModelFactory
import com.example.cinephile.ui.search.MovieAdapter
import kotlinx.coroutines.launch

// RENAME: This is now WatchlistDetailFragment
class WatchlistDetailFragment : Fragment(R.layout.fragment_watchlist) {

    private lateinit var viewModel: WatchlistViewModel
    private lateinit var movieAdapter: MovieAdapter

    // VARIABLE: To track which list we are looking at
    private var currentListId: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. GET ARGUMENTS (Passed from the Manager Screen)
        currentListId = arguments?.getLong("listId") ?: 0L
        val listName = arguments?.getString("listName") ?: "Watchlist"

        // 2. Set the Header Title (e.g. "Horror Movies")
        view.findViewById<TextView>(R.id.tvHeader).text = listName

        // 3. Setup ViewModel
        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[WatchlistViewModel::class.java]

        // 4. Find Views
        val rvWatchlist = view.findViewById<RecyclerView>(R.id.rvWatchlist)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layoutEmptyState)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        // 5. Setup Adapter (Your existing logic)
        movieAdapter = MovieAdapter(
            onMovieClick = { movie ->
                val bundle = Bundle().apply { putInt("movieId", movie.id) }
                findNavController().navigate(R.id.action_global_detailsFragment, bundle)
            },
            onMovieLongClick = { movie ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Remove Movie")
                    .setMessage("Remove '${movie.title}' from this list?")
                    .setPositiveButton("Yes") { _, _ ->

                        // --- FIX: Pass 'currentListId' here ---
                        viewModel.removeFromWatchlist(movie, currentListId)

                        Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show()

                        // (You don't need to call loadCustomList here anymore because
                        // the ViewModel does it automatically now)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        )

        rvWatchlist.layoutManager = GridLayoutManager(context, 3)
        rvWatchlist.adapter = movieAdapter

        // 6. Observe Data
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is WatchlistUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        rvWatchlist.visibility = View.GONE
                        layoutEmpty.visibility = View.GONE
                    }
                    is WatchlistUiState.Empty -> {
                        progressBar.visibility = View.GONE
                        rvWatchlist.visibility = View.GONE
                        layoutEmpty.visibility = View.VISIBLE
                    }
                    is WatchlistUiState.Success -> {
                        progressBar.visibility = View.GONE
                        layoutEmpty.visibility = View.GONE
                        rvWatchlist.visibility = View.VISIBLE
                        movieAdapter.submitList(state.movies)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // CRITICAL CHANGE: Load the specific custom list, not just the default one
        viewModel.loadCustomList(currentListId)
    }
}