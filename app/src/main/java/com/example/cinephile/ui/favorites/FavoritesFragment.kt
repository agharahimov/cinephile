package com.example.cinephile.ui.favorites

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
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
import com.example.cinephile.ui.search.MovieAdapter
import com.example.cinephile.ui.watchlist.WatchlistUiState
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private lateinit var viewModel: FavoritesViewModel
    private lateinit var movieAdapter: MovieAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Change Header Text
        view.findViewById<TextView>(R.id.tvHeader).text = "Your Favorites"

        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[FavoritesViewModel::class.java]

        val rvList = view.findViewById<RecyclerView>(R.id.rvFavorites)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layoutEmptyState)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        movieAdapter = MovieAdapter(
            onMovieClick = { movie ->
                val bundle = Bundle().apply { putInt("movieId", movie.id) }
                findNavController().navigate(R.id.action_global_detailsFragment, bundle)
            },
            onMovieLongClick = { movie ->
                // --- CUSTOM DARK DIALOG LOGIC ---

                // 1. Inflate the custom view
                val dialogView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_custom_confirm, null)

                // 2. Create the Dialog
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create()

                // 3. Make background transparent (for rounded corners)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                // 4. Setup Text
                dialogView.findViewById<TextView>(R.id.tvDialogTitle).text = "Remove Favorite"
                dialogView.findViewById<TextView>(R.id.tvDialogMessage).text =
                    "Remove '${movie.title}' from your favorites?"

                // 5. Setup Buttons
                val btnCancel = dialogView.findViewById<View>(R.id.btnDialogCancel)
                val btnConfirm = dialogView.findViewById<View>(R.id.btnDialogConfirm)

                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                btnConfirm.setOnClickListener {
                    // Perform Delete Action
                    viewModel.removeFromFavorites(movie)
                    Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }

                dialog.show()
            }
        )

        rvList.layoutManager = GridLayoutManager(context, 2)
        rvList.adapter = movieAdapter

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is WatchlistUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        rvList.visibility = View.GONE
                        layoutEmpty.visibility = View.GONE
                    }
                    is WatchlistUiState.Empty -> {
                        progressBar.visibility = View.GONE
                        rvList.visibility = View.GONE
                        layoutEmpty.visibility = View.VISIBLE
                    }
                    is WatchlistUiState.Success -> {
                        progressBar.visibility = View.GONE
                        layoutEmpty.visibility = View.GONE
                        rvList.visibility = View.VISIBLE
                        movieAdapter.submitList(state.movies)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites()
    }
}