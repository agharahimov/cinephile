package com.example.cinephile.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.cinephile.R
import com.example.cinephile.ui.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ActionBottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: DetailsViewModel
    private var movieId: Int = 0
    private var movieTitle: String = ""
    private var movieDate: String = ""

    // Static method to create new instance with data
    companion object {
        fun newInstance(movieId: Int, title: String, date: String): ActionBottomSheet {
            val args = Bundle()
            args.putInt("id", movieId)
            args.putString("title", title)
            args.putString("date", date)
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

        // 1. Get Data
        movieId = arguments?.getInt("id") ?: 0
        movieTitle = arguments?.getString("title") ?: ""
        movieDate = arguments?.getString("date") ?: ""

        // 2. Connect to Shared ViewModel
        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(requireParentFragment(), factory)[DetailsViewModel::class.java] // requireParentFragment() shares state with DetailsFragment

        // 3. Setup UI
        view.findViewById<TextView>(R.id.tvSheetTitle).text = movieTitle
        view.findViewById<TextView>(R.id.tvSheetYear).text = movieDate.take(4) // Just Year

        val btnLike = view.findViewById<LinearLayout>(R.id.btnSheetLike)
        val btnWatchlist = view.findViewById<LinearLayout>(R.id.btnSheetWatchlist)
        val ivLike = view.findViewById<ImageView>(R.id.ivSheetLike)
        val ivWatchlist = view.findViewById<ImageView>(R.id.ivSheetWatchlist)

        // 4. Click Listeners
        btnLike.setOnClickListener {
            viewModel.toggleFavorite()
            Toast.makeText(context, "Like Toggled", Toast.LENGTH_SHORT).show()
            dismiss() // Close sheet after action
        }

        btnWatchlist.setOnClickListener {
            viewModel.toggleWatchlist()
            Toast.makeText(context, "Watchlist Toggled", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        // Optional: Update icons based on state immediately if you want dynamic icons here
    }
}