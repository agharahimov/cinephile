package com.example.cinephile.ui.details

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.cinephile.R
import com.example.cinephile.ui.ViewModelFactory
import kotlinx.coroutines.launch

class DetailsFragment : Fragment(R.layout.fragment_details) {

    private lateinit var viewModel: DetailsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val movieId = arguments?.getInt("movieId") ?: 0

        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[DetailsViewModel::class.java]

        if (movieId != 0) viewModel.loadMovie(movieId)

        // --- 1. UI References ---
        val ivBackdrop = view.findViewById<ImageView>(R.id.ivBackdrop)
        val ivSmallPoster = view.findViewById<ImageView>(R.id.ivSmallPoster)
        val tvTitle = view.findViewById<TextView>(R.id.tvDetailTitle)
        val tvDate = view.findViewById<TextView>(R.id.tvDetailDate)
        val tvOverview = view.findViewById<TextView>(R.id.tvDetailOverview)
        val btnBack = view.findViewById<View>(R.id.btnBack)
        val tvRating = view.findViewById<TextView>(R.id.tvRatingText)
        val bottomActionContainer = view.findViewById<View>(R.id.bottomActionContainer)

        // --- 2. LOGIC ---
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // --- THE FIX IS HERE ---
        bottomActionContainer.setOnClickListener {
            val currentMovie = viewModel.uiState.value.movie
            if (currentMovie != null) {
                // FIXED: Pass the whole 'currentMovie' object, not 3 separate variables
                val bottomSheet = ActionBottomSheet.newInstance(currentMovie)
                bottomSheet.show(parentFragmentManager, "ActionSheet")
            }
        }

        // --- 3. UPDATE UI WITH DATA ---
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.movie?.let { movie ->
                    tvTitle.text = movie.title

                    // Date formatting logic
                    val year = if (movie.releaseDate.length >= 4) movie.releaseDate.take(4) else "Unknown"
                    if (movie.director.isNotEmpty()) {
                        tvDate.text = "$year • ${movie.director}"
                    } else {
                        tvDate.text = year
                    }

                    tvOverview.text = movie.overview
                    tvRating.text = "TMDB: ${String.format("%.1f", movie.rating)}"

                    // Load Images
                    ivBackdrop.load(movie.backdropUrl) {
                        crossfade(true)
                        placeholder(android.R.drawable.ic_menu_gallery)
                        error(android.R.color.darker_gray)
                    }

                    ivSmallPoster.load(movie.posterUrl) {
                        crossfade(true)
                    }
                }
            }
        }
    }
}