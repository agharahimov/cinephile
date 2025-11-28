package com.example.cinephile

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cinephile.ui.ViewModelFactory
import com.example.cinephile.ui.home.HomeUiState
import com.example.cinephile.ui.home.RecommendationViewModel
import com.example.cinephile.ui.search.MovieAdapter
import kotlinx.coroutines.launch

class RecommendationFragment : Fragment(R.layout.fragment_recommendation) {

    private lateinit var viewModel: RecommendationViewModel
    private lateinit var adapter: MovieAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[RecommendationViewModel::class.java]

        val rvMovies = view.findViewById<RecyclerView>(R.id.rvRecommendations) // Need to add ID to XML
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar) // Need to add ID
        val tvError = view.findViewById<TextView>(R.id.tvError) // Need to add ID

        adapter = MovieAdapter(
            onMovieClick = { movie ->
                val bundle = Bundle().apply { putInt("movieId", movie.id) }
                findNavController().navigate(R.id.action_global_detailsFragment, bundle)
            },
            onMovieLongClick = {
                // Add your "Smart Add" logic here if you want!
            }
        )

        rvMovies.layoutManager = GridLayoutManager(context, 2)
        rvMovies.adapter = adapter

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is HomeUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        rvMovies.visibility = View.GONE
                        tvError.visibility = View.GONE
                    }
                    is HomeUiState.Success -> {
                        progressBar.visibility = View.GONE
                        rvMovies.visibility = View.VISIBLE
                        adapter.submitList(state.movies)
                    }
                    is HomeUiState.Error -> {
                        progressBar.visibility = View.GONE
                        tvError.visibility = View.VISIBLE
                        tvError.text = state.message
                    }
                }
            }
        }
    }
}