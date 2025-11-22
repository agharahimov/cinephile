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

        // 1. Get Movie ID passed from previous screen
        val movieId = arguments?.getInt("movieId") ?: 0

        // 2. Setup ViewModel
        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[DetailsViewModel::class.java]

        // Load data
        if (movieId != 0) viewModel.loadMovie(movieId)

        // 3. UI References
        val ivPoster = view.findViewById<ImageView>(R.id.ivDetailPoster)
        val tvTitle = view.findViewById<TextView>(R.id.tvDetailTitle)
        val tvDate = view.findViewById<TextView>(R.id.tvDetailDate)
        val tvOverview = view.findViewById<TextView>(R.id.tvDetailOverview)
        val btnFav = view.findViewById<Button>(R.id.btnFavorite)
        val btnWatch = view.findViewById<Button>(R.id.btnWatchlist)
        val btnBack = view.findViewById<View>(R.id.btnBack)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)

        // 4. Back Button Logic
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // 5. Button Logic
        btnFav.setOnClickListener { viewModel.toggleFavorite() }
        btnWatch.setOnClickListener { viewModel.toggleWatchlist() }

        // 6. Observe Data
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Update Text/Image
                state.movie?.let { movie ->
                    tvTitle.text = movie.title
                    tvDate.text = movie.releaseDate
                    tvOverview.text = movie.overview
                    ratingBar.rating = (movie.rating / 2).toFloat() // TMDB is 10, Bar is 5

                    ivPoster.load(movie.posterUrl) { crossfade(true) }
                }

                // Update Button Colors/Text
                if (state.isFavorite) {
                    btnFav.text = "Liked"
                    btnFav.setBackgroundColor(0xFFE91E63.toInt()) // Pink
                } else {
                    btnFav.text = "Favorite"
                    btnFav.setBackgroundColor(0xFF333333.toInt()) // Dark Gray
                }

                if (state.isInWatchlist) {
                    btnWatch.text = "Saved"
                    btnWatch.setBackgroundColor(0xFF03DAC5.toInt()) // Teal
                } else {
                    btnWatch.text = "Watchlist"
                    btnWatch.setBackgroundColor(0xFF6200EE.toInt()) // Purple
                }
            }
        }
    }
}