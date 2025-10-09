package com.example.deezerapidemo.network

import com.example.deezerapidemo.model.DeezerSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DeezerApiInterface {
    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): DeezerSearchResponse
}