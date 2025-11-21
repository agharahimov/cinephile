package com.example.cinephile.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cinephile.R
import com.example.cinephile.domain.model.Movie

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

        fun bind(movie: Movie, onClick: (Movie) -> Unit, onLongClick: (Movie) -> Unit) {
            tvTitle.text = movie.title
            // Assuming your Movie model has releaseDate or similar
            tvInfo.text = "${movie.releaseDate}"

            // Note: In a real app, use a library like Glide or Coil here
            // Glide.with(itemView).load(movie.posterUrl).into(ivPoster)
            ivPoster.setImageResource(android.R.drawable.ic_menu_gallery) // Placeholder

            itemView.setOnClickListener { onClick(movie) }
            itemView.setOnLongClickListener {
                onLongClick(movie)
                true // Return true to indicate the click was handled
            }
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
}