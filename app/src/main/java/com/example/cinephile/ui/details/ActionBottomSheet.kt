package com.example.cinephile.ui.details

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.cinephile.R
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.ui.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ActionBottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: DetailsViewModel
    private lateinit var movieObj: Movie

    companion object {
        fun newInstance(movie: Movie): ActionBottomSheet {
            val args = Bundle()
            args.putInt("id", movie.id)
            args.putString("title", movie.title)
            args.putString("date", movie.releaseDate)
            args.putString("poster", movie.posterUrl)
            args.putString("backdrop", movie.backdropUrl)
            args.putString("overview", movie.overview)
            args.putDouble("rating", movie.rating)
            val fragment = ActionBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Reconstruct Movie Object
        val args = arguments ?: return
        movieObj = Movie(
            id = args.getInt("id"),
            title = args.getString("title") ?: "",
            releaseDate = args.getString("date") ?: "",
            posterUrl = args.getString("poster") ?: "",
            backdropUrl = args.getString("backdrop") ?: "",
            overview = args.getString("overview") ?: "",
            rating = args.getDouble("rating"),
            director = ""
        )

        // 2. Connect to Parent ViewModel (Shares state with DetailsFragment)
        // This ensures we see the SAME data (Liked/Watchlisted) as the screen behind us
        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(requireParentFragment(), factory)[DetailsViewModel::class.java]

        // 3. UI References
        val tvTitle = view.findViewById<TextView>(R.id.tvSheetTitle)
        val tvYear = view.findViewById<TextView>(R.id.tvSheetYear)

        val btnLike = view.findViewById<LinearLayout>(R.id.btnSheetLike)
        val ivLike = view.findViewById<ImageView>(R.id.ivSheetLike)
        val tvLike = view.findViewById<TextView>(R.id.tvSheetLike)

        val btnWatchlist = view.findViewById<LinearLayout>(R.id.btnSheetWatchlist)
        val ivWatchlist = view.findViewById<ImageView>(R.id.ivSheetWatchlist)
        val tvWatchlist = view.findViewById<TextView>(R.id.tvSheetWatchlist)

        // Set Static Data
        tvTitle.text = movieObj.title
        tvYear.text = movieObj.releaseDate.take(4)

        // 4. OBSERVE STATE (The Fix)
        // This runs immediately when sheet opens and checks if movie is already liked
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->

                // --- UPDATE LIKE ICON ---
                if (state.isFavorite) {
                    ivLike.setImageResource(android.R.drawable.star_big_on) // Filled Star
                    ivLike.imageTintList = ColorStateList.valueOf(Color.parseColor("#E91E63")) // Red
                    tvLike.text = "Liked"
                    tvLike.setTextColor(Color.parseColor("#E91E63"))
                } else {
                    ivLike.setImageResource(android.R.drawable.btn_star) // Outline Star
                    ivLike.imageTintList = ColorStateList.valueOf(Color.parseColor("#99AABB")) // Grey
                    tvLike.text = "Like"
                    tvLike.setTextColor(Color.parseColor("#99AABB"))
                }

                // --- UPDATE WATCHLIST ICON ---
                if (state.isInWatchlist) {
                    ivWatchlist.setImageResource(android.R.drawable.checkbox_on_background) // Checkmark
                    ivWatchlist.imageTintList = ColorStateList.valueOf(Color.parseColor("#03DAC5")) // Teal
                    tvWatchlist.text = "Saved"
                    tvWatchlist.setTextColor(Color.parseColor("#03DAC5"))
                } else {
                    ivWatchlist.setImageResource(android.R.drawable.ic_input_add) // Plus Sign
                    ivWatchlist.imageTintList = ColorStateList.valueOf(Color.parseColor("#99AABB")) // Grey
                    tvWatchlist.text = "Watchlist"
                    tvWatchlist.setTextColor(Color.parseColor("#99AABB"))
                }
            }
        }

        // 5. Click Listeners
        btnLike.setOnClickListener {
            viewModel.toggleFavorite(movieObj)
            // Don't dismiss, let user see the change
        }

        btnWatchlist.setOnClickListener {
            viewModel.toggleWatchlist(movieObj)
            // Dismiss after adding to watchlist feels natural
            dismiss()
            Toast.makeText(context, "Watchlist Updated", Toast.LENGTH_SHORT).show()
        }
    }
}