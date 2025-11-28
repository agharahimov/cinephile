package com.example.cinephile.ui.details

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent // <--- Added Import
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cinephile.R
import com.example.cinephile.domain.model.Movie
import com.example.cinephile.ui.ViewModelFactory
import com.example.cinephile.util.GuestManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ActionBottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: DetailsViewModel
    private lateinit var movieObj: Movie

    // Track if we showed the rating message so it doesn't spam
    private var hasShownRatingToast = false

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
            // Handle genre list safely
            val genreList = ArrayList(movie.genres ?: emptyList())
            args.putIntegerArrayList("genres", genreList)

            val fragment = ActionBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_actions, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments ?: return
        val genreList = args.getIntegerArrayList("genres")?.toList() ?: emptyList()

        movieObj = Movie(
            id = args.getInt("id"),
            title = args.getString("title") ?: "",
            releaseDate = args.getString("date") ?: "",
            posterUrl = args.getString("poster") ?: "",
            backdropUrl = args.getString("backdrop") ?: "",
            overview = args.getString("overview") ?: "",
            rating = args.getDouble("rating"),
            director = "",
            genres = genreList
        )

        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(requireParentFragment(), factory)[DetailsViewModel::class.java]
        viewModel.checkDatabaseStatus(movieObj.id)

        // UI References
        view.findViewById<TextView>(R.id.tvSheetTitle).text = movieObj.title
        view.findViewById<TextView>(R.id.tvSheetYear).text = movieObj.releaseDate.take(4)

        val btnLike = view.findViewById<LinearLayout>(R.id.btnSheetLike)
        val ivLike = view.findViewById<ImageView>(R.id.ivSheetLike)
        val tvLike = view.findViewById<TextView>(R.id.tvSheetLike)

        val btnWatchlist = view.findViewById<LinearLayout>(R.id.btnSheetWatchlist)
        val ivWatchlist = view.findViewById<ImageView>(R.id.ivSheetWatchlist)
        val tvWatchlist = view.findViewById<TextView>(R.id.tvSheetWatchlist)

        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBarSheet)

        // ===========================================================================
        // 1. GUEST BLOCKER FOR RATING (The Gatekeeper)
        // ===========================================================================
        ratingBar.setOnTouchListener { _, event ->
            // Check immediately when the user touches the bar
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (GuestManager.isGuest(requireContext())) {
                    // IF GUEST: Show the "Login Required" Dialog
                    GuestManager.checkAndRun(requireContext(), findNavController()) {
                        // This block won't run because they are a guest,
                        // but the dialog will show.
                    }
                    // RETURN TRUE: This stops the touch event here.
                    // The RatingBar will NOT update visually.
                    return@setOnTouchListener true
                }
            }
            // IF USER: Return false. This lets the touch pass through to the
            // RatingBar so they can drag/click the stars normally.
            false
        }

        // ===========================================================================
        // 2. RATING LISTENER (Logic for Logged-in Users)
        // ===========================================================================
        ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->

            // --- FIX: SECOND SECURITY CHECK ---
            // If for some reason the touch listener didn't block it, check here again.
            if (GuestManager.isGuest(requireContext())) {
                // Reset the bar visually to 0 (optional, so it doesn't look rated)
                // ratingBar.rating = 0f

                // STOP. Do not save to DB. Do not add to Watchlist.
                return@setOnRatingBarChangeListener
            }

            if (fromUser) {
                // 1. Create copy with new rating
                val ratedMovie = movieObj.copy(userRating = rating.toDouble())

                // 2. Save Rating to DB
                viewModel.rateMovie(ratedMovie)

                // 3. ALGORITHM: Auto-Add to Watchlist
                val currentState = viewModel.uiState.value
                if (!currentState.isInWatchlist) {
                    viewModel.toggleWatchlist(ratedMovie)
                }

                // 4. Feedback
                if (!hasShownRatingToast) {
                    Toast.makeText(context, "Rated $rating stars & Added to Watchlist", Toast.LENGTH_SHORT).show()
                    hasShownRatingToast = true
                }
            }
        }

        // 4. OBSERVE STATE
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->

                state.userRating?.let {
                    ratingBar.rating = it.toFloat()
                }

                // --- UPDATE LIKE VISUALS ---
                if (state.isFavorite) {
                    ivLike.setImageResource(android.R.drawable.star_big_on)
                    ivLike.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFD700"))
                    tvLike.text = "Liked"
                    tvLike.setTextColor(Color.parseColor("#FFD700"))
                } else {
                    ivLike.setImageResource(android.R.drawable.btn_star)
                    ivLike.imageTintList = ColorStateList.valueOf(Color.parseColor("#99AABB"))
                    tvLike.text = "Like"
                    tvLike.setTextColor(Color.parseColor("#99AABB"))
                }

                // --- UPDATE WATCHLIST VISUALS ---
                if (state.isInWatchlist) {
                    ivWatchlist.setImageResource(R.drawable.ic_check)
                    ivWatchlist.imageTintList = ColorStateList.valueOf(Color.parseColor("#03DAC5"))
                    tvWatchlist.text = "Added"
                    tvWatchlist.setTextColor(Color.parseColor("#03DAC5"))
                } else {
                    ivWatchlist.setImageResource(android.R.drawable.ic_input_add)
                    ivWatchlist.imageTintList = ColorStateList.valueOf(Color.parseColor("#99AABB"))
                    tvWatchlist.text = "Watchlist"
                    tvWatchlist.setTextColor(Color.parseColor("#99AABB"))
                }
            }
        }

        // 5. CLICK LISTENERS (With Guest Checks)
        btnLike.setOnClickListener {
            GuestManager.checkAndRun(requireContext(), findNavController()) {
                viewModel.toggleFavorite(movieObj)
            }
        }

        btnWatchlist.setOnClickListener {
            GuestManager.checkAndRun(requireContext(), findNavController()) {
                // Check if it's already in watchlist to toggle removal,
                // or show the add dialog if you are using lists
                val uiState = viewModel.uiState.value
                if (uiState.isInWatchlist) {
                    viewModel.toggleWatchlist(movieObj)
                    Toast.makeText(context, "Removed from Watchlist", Toast.LENGTH_SHORT).show()
                } else {
                    showAddToListDialog(movieObj)
                }
            }
        }
    }

    private fun showAddToListDialog(movie: Movie) {
        viewModel.getUserLists { lists ->
            if (lists.isEmpty()) {
                viewModel.toggleWatchlist(movie)
                Toast.makeText(context, "Added to Default Watchlist", Toast.LENGTH_SHORT).show()
                return@getUserLists
            }

            if (lists.size == 1) {
                val list = lists[0]
                viewModel.addMovieToSpecificList(movie, list.listId)
                Toast.makeText(context, "Added to ${list.name}", Toast.LENGTH_SHORT).show()
                return@getUserLists
            }

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