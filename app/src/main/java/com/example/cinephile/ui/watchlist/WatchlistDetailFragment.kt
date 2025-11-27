package com.example.cinephile.ui.watchlist

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class WatchlistDetailFragment : Fragment(R.layout.fragment_watchlist) {

    private lateinit var viewModel: WatchlistViewModel
    private lateinit var movieAdapter: MovieAdapter

    private var currentListId: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. GET ARGUMENTS
        currentListId = arguments?.getLong("listId") ?: 0L
        val listName = arguments?.getString("listName") ?: "Watchlist"

        // 2. Set Title
        view.findViewById<TextView>(R.id.tvHeader).text = listName

        // 3. Setup ViewModel
        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[WatchlistViewModel::class.java]

        // 4. Find Views
        val rvWatchlist = view.findViewById<RecyclerView>(R.id.rvWatchlist)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layoutEmptyState)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        // 5. Setup Adapter
        movieAdapter = MovieAdapter(
            onMovieClick = { movie ->
                val bundle = Bundle().apply { putInt("movieId", movie.id) }
                findNavController().navigate(R.id.action_global_detailsFragment, bundle)
            },
            onMovieLongClick = { movie ->
                // --- NEW CUSTOM DIALOG LOGIC ---

                // 1. Inflate custom layout
                val dialogView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_custom_confirm, null)

                // 2. Build Dialog
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create()

                // 3. Transparent background for rounded corners
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                // 4. Set Text
                dialogView.findViewById<TextView>(R.id.tvDialogTitle).text = "Remove Movie"
                dialogView.findViewById<TextView>(R.id.tvDialogMessage).text =
                    "Remove '${movie.title}' from this list?"

                // 5. Handle Buttons
                val btnCancel = dialogView.findViewById<View>(R.id.btnDialogCancel)
                val btnConfirm = dialogView.findViewById<View>(R.id.btnDialogConfirm)

                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                btnConfirm.setOnClickListener {
                    // Perform Delete (pass currentListId to remove from THIS list specifically)
                    viewModel.removeFromWatchlist(movie, currentListId)
                    Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }

                dialog.show()
            }
        )

        // 3 Columns (Matches your previous preference)
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
        viewModel.loadCustomList(currentListId)
    }
}