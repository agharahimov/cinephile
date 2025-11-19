package com.example.deezerapidemo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.deezerapidemo.R
import com.example.deezerapidemo.model.Track

class TrackAdapter(private val tracks: List<Track>) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    inner class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val albumCoverImageView: ImageView = view.findViewById(R.id.albumCoverImageView)
        val albumTitleTextView: TextView = view.findViewById(R.id.albumTitleTextView)
        val songTitleTextView: TextView = view.findViewById(R.id.songTitleTextView)
        val artistNameTextView: TextView = view.findViewById(R.id.artistNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_item, parent, false)
        return TrackViewHolder(view)
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]

        holder.albumTitleTextView.text = track.album.title
        holder.songTitleTextView.text = track.title
        holder.artistNameTextView.text = track.artist.name

        Glide.with(holder.itemView.context)
            .load(track.album.cover)
            .into(holder.albumCoverImageView)
    }
}