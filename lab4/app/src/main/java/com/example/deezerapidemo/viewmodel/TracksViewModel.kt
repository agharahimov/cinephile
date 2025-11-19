package com.example.deezerapidemo.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deezerapidemo.model.DeezerSearchResponse
import com.example.deezerapidemo.network.DeezerApiInterface
import com.example.deezerapidemo.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TracksViewModel : ViewModel() {

    private val _data = MutableLiveData<DeezerSearchResponse>()

    val data: MutableLiveData<DeezerSearchResponse> = _data

    private val apiInterface: DeezerApiInterface =
        RetrofitInstance.getInstance().create(DeezerApiInterface::class.java)

    fun search(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiInterface.searchTracks(query)
                _data.postValue(response)
            } catch (e: Exception) {
                Log.e("TracksViewModel", "API call failed: ${e.message}")
            }
        }
    }
}