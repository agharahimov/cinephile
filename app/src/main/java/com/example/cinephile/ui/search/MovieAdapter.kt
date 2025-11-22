package com.example.cinephile.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.cinephile.R
import com.example.cinephile.domain.model.Movie
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MovieAdapter(
    private val onMovieClick: (Movie) -> Unit,
    private val onMovieLongClick: (Movie) -> Unit
) : ListAdapter<Movie, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_grid, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = getItem(position)
        holder.bind(movie, onMovieClick, onMovieLongClick)
    }

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPoster: ImageView = itemView.findViewById(R.id.ivMoviePoster)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvMovieTitle)
        private val tvInfo: TextView = itemView.findViewById(R.id.tvMovieInfo)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)

        fun bind(movie: Movie, onClick: (Movie) -> Unit, onLongClick: (Movie) -> Unit) {
            tvTitle.text = movie.title

            // 1. DATE: Shows "10 Nov 2025"
            tvInfo.text = formatDate(movie.releaseDate)

            // 2. RATING: Shows "★ 7.5"
            val ratingText = String.format("%.1f", movie.rating)
            tvRating.text = "★ $ratingText"

            // 3. IMAGE
            ivPoster.load(movie.posterUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_report_image)
            }

            itemView.setOnClickListener { onClick(movie) }
            itemView.setOnLongClickListener {
                onLongClick(movie)
                true
            }
        }

        private fun formatDate(dateString: String): String {
            if (dateString.isBlank() || dateString == "Unknown") return "" // Return empty if unknown

            return try {
                // Parse "2025-11-10"
                val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                val date = LocalDate.parse(dateString, inputFormat)

                // Convert to "10 Nov 2025" (MMM = Short Month)
                val outputFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
                date.format(outputFormat)
            } catch (e: Exception) {
                dateString // Fallback to original if error
            }
        }
    }


    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Movie, newItem: Movie) = oldItem == newItem
    }
}
