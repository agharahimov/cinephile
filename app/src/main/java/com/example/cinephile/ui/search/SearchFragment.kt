package com.example.cinephile.ui.search

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
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var viewModel: SearchViewModel
    private lateinit var movieAdapter: MovieAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. INIT VIEWMODEL (Using teammate's updated Factory)
        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[SearchViewModel::class.java]

        // 2. SETUP UI REFERENCES
        val etSearch = view.findViewById<EditText>(R.id.etSearchQuery)
        val btnSearch = view.findViewById<View>(R.id.btnSearch)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvError = view.findViewById<TextView>(R.id.tvError)
        val rvMovies = view.findViewById<RecyclerView>(R.id.rvMovies)

        // 3. SETUP RECYCLERVIEW (Grid Layout - 2 Columns)
        movieAdapter = MovieAdapter(
            onMovieClick = { movie ->
                // Navigate to Details Screen (Pass ID)
                // val action = SearchFragmentDirections.actionSearchToDetails(movie.id)
                // findNavController().navigate(action)
                Toast.makeText(context, "Clicked: ${movie.title}", Toast.LENGTH_SHORT).show()
            },
            onMovieLongClick = { movie ->
                // Add to Watchlist Logic
                Toast.makeText(context, "Added to Watchlist: ${movie.title}", Toast.LENGTH_SHORT).show()
            }
        )

        rvMovies.layoutManager = GridLayoutManager(context, 2) // 2 columns
        rvMovies.adapter = movieAdapter

        // 4. OBSERVE STATE (Using teammate's SearchUiState)
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

        // 5. HANDLE SEARCH INPUT
        btnSearch.setOnClickListener {
            val query = etSearch.text.toString()
            if (query.isNotBlank()) {
                viewModel.searchMovies(query)
                // Hide keyboard if you have a utility for that
            }
        }
    }
}