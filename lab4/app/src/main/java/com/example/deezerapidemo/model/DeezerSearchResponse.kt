package com.example.deezerapidemo.model

data class DeezerSearchResponse(
    val data: List<Track>
)

data class Track(
    val id: String,
    val title: String,
    val duration: String,
    val artist: Artist,
    val album: Album
)

data class Album(
    val id: String,
    val title: String,
    val cover: String
    // Removed the 'tracks' list to prevent circular dependencies
)

data class Artist(
    val id: String,
    val name: String,
    val picture: String
)