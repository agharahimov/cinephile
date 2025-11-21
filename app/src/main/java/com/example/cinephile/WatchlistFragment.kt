package com.example.cinephile

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cinephile.ui.ViewModelFactory
import com.example.cinephile.ui.search.MovieAdapter // Reuse the adapter from Search
import com.example.cinephile.ui.watchlist.WatchlistUiState
import com.example.cinephile.ui.watchlist.WatchlistViewModel
import kotlinx.coroutines.launch

class WatchlistFragment : Fragment(R.layout.fragment_watchlist) {

    private lateinit var viewModel: WatchlistViewModel
    private lateinit var movieAdapter: MovieAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup ViewModel
        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[WatchlistViewModel::class.java]

        // 2. Find Views
        val rvWatchlist = view.findViewById<RecyclerView>(R.id.rvWatchlist)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layoutEmptyState)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        // 3. Setup Adapter (Reuse logic)
        movieAdapter = MovieAdapter(
            onMovieClick = { movie ->
                Toast.makeText(context, "Clicked: ${movie.title}", Toast.LENGTH_SHORT).show()
            },
            onMovieLongClick = { movie ->
                // Show "Remove" dialog
                AlertDialog.Builder(requireContext())
                    .setTitle("Remove Movie")
                    .setMessage("Remove '${movie.title}' from watchlist?")
                    .setPositiveButton("Yes") { _, _ ->
                        viewModel.removeFromWatchlist(movie)
                        Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        )

        // 3 Columns for a grid look
        rvWatchlist.layoutManager = GridLayoutManager(context, 3)
        rvWatchlist.adapter = movieAdapter

        // 4. Observe Data
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
        // Refresh list when returning to this tab
        viewModel.loadWatchlist()
    }
}