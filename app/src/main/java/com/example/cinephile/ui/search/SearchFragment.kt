package com.example.cinephile.ui.search

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController // Required for navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cinephile.R
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.ui.ViewModelFactory
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var viewModel: SearchViewModel
    private lateinit var movieAdapter: MovieAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. INIT VIEWMODEL
        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[SearchViewModel::class.java]

        // 2. SETUP UI REFERENCES
        val etSearch = view.findViewById<EditText>(R.id.etSearchQuery)
        val btnSearch = view.findViewById<View>(R.id.btnSearch)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvError = view.findViewById<TextView>(R.id.tvError)
        val rvMovies = view.findViewById<RecyclerView>(R.id.rvMovies)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupFilters)

        // 3. SETUP RECYCLERVIEW
        movieAdapter = MovieAdapter(
            onMovieClick = { movie ->
                val bundle = Bundle().apply { putInt("movieId", movie.id) }
                findNavController().navigate(R.id.action_global_detailsFragment, bundle)
            },
            onMovieLongClick = { movie ->
                // --- LOGIC ADDED HERE ---
                viewModel.addToWatchlist(movie)
                Toast.makeText(context, "${movie.title} added to Watchlist", Toast.LENGTH_SHORT).show()
            }
        )

        rvMovies.layoutManager = GridLayoutManager(context, 2)
        rvMovies.adapter = movieAdapter

        // 4. OBSERVE STATE
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is SearchUiState.Idle -> {
                        progressBar.visibility = View.GONE
                        tvError.visibility = View.GONE
                        rvMovies.visibility = View.VISIBLE
                    }
                    is SearchUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        tvError.visibility = View.GONE
                        rvMovies.visibility = View.GONE
                    }
                    is SearchUiState.Success -> {
                        progressBar.visibility = View.GONE
                        rvMovies.visibility = View.VISIBLE
                        movieAdapter.submitList(state.movies)

                        if (state.movies.isEmpty()) {
                            tvError.text = "No movies found"
                            tvError.visibility = View.VISIBLE
                        } else {
                            tvError.visibility = View.GONE
                        }
                    }
                    is SearchUiState.Error -> {
                        progressBar.visibility = View.GONE
                        tvError.text = state.message
                        tvError.visibility = View.VISIBLE
                    }
                }
            }
        }

        // 5. FILTER LOGIC (UI UX)
        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            etSearch.hint = when (checkedId) {
                R.id.chipYear -> "Enter Year (e.g. 2023)"
                R.id.chipDirector -> "Enter Director Name"
                else -> "Search movies..."
            }
        }

        // 6. HANDLE SEARCH CLICK
        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()

            if (query.isNotBlank()) {
                // Determine which Filter is active
                val searchType = when (chipGroup.checkedChipId) {
                    R.id.chipYear -> SearchType.YEAR
                    R.id.chipGenre -> SearchType.GENRE
                    R.id.chipDirector -> SearchType.DIRECTOR
                    else -> SearchType.TITLE
                }

                // Validation for YEAR
                if (searchType == SearchType.YEAR) {
                    if (query.length != 4 || !query.all { it.isDigit() }) {
                        Toast.makeText(context, "Please enter a valid 4-digit year", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }

                // Pass Query AND Type to ViewModel
                viewModel.searchMovies(query, searchType)

                // Optional: Hide keyboard here
            }
        }
    }
}